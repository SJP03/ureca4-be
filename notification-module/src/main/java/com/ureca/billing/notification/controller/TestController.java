package com.ureca.billing.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.billing.core.dto.BillingMessageDto;
import com.ureca.billing.notification.service.EmailService;
import com.ureca.billing.notification.service.MessagePolicyService;
import com.ureca.billing.notification.service.WaitingQueueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.Map;

@Tag(name = "Test", description = "이메일 발송 테스트 API")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    private final MessagePolicyService policyService;
    private final WaitingQueueService queueService;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 통합 테스트: 현재 실제 시간으로 발송
     */
    @Operation(summary = "이메일 발송 테스트", description = "Kafka 거치지 않고 직접 이메일 발송 테스트")
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> testSend(@RequestBody BillingMessageDto message) {
        LocalTime now = LocalTime.now();
        log.info("🧪 테스트 발송 요청. billId={}, currentTime={}", message.getBillId(), now);
        
        boolean isBlock = policyService.isBlockTime();
        
        if (isBlock) {
            try {
                String messageJson = objectMapper.writeValueAsString(message);
                queueService.addToQueue(messageJson);
            } catch (Exception e) {
                log.error("JSON 변환 실패", e);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "action", "QUEUED",
                "message", "⏰ 금지 시간입니다. 대기열에 저장되었습니다.",
                "currentTime", now.toString()
            ));
        }
        
        try {
            emailService.sendEmail(message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "action", "SENT",
                "message", "✅ 이메일이 즉시 발송되었습니다.",
                "currentTime", now.toString()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "action", "FAILED",
                "message", "❌ 발송 실패: " + e.getMessage(),
                "currentTime", now.toString()
            ));
        }
    }
    
    /**
     * 통합 테스트: 시뮬레이션 시간으로 발송
     */
    @Operation(summary = "시뮬레이션 시간으로 발송")
    @PostMapping("/send-with-time")
    public ResponseEntity<Map<String, Object>> testSendWithTime(
            @RequestBody BillingMessageDto message,
            @RequestParam String simulatedTime) {
        
        LocalTime testTime = LocalTime.parse(simulatedTime);
        LocalTime actualTime = LocalTime.now();
        log.info("🧪 시뮬레이션 테스트. simulatedTime={}, actualTime={}", testTime, actualTime);
        
        boolean isBlock = policyService.isBlockTime(testTime);
        
        if (isBlock) {
            try {
                String messageJson = objectMapper.writeValueAsString(message);
                queueService.addToQueue(messageJson);
            } catch (Exception e) {
                log.error("JSON 변환 실패", e);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "action", "QUEUED",
                "message", "⏰ 금지 시간입니다. 대기열에 저장되었습니다.",
                "simulatedTime", testTime.toString(),
                "actualTime", actualTime.toString()
            ));
        }
        
        try {
            emailService.sendEmail(message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "action", "SENT",
                "message", "✅ 이메일이 즉시 발송되었습니다.",
                "simulatedTime", testTime.toString(),
                "actualTime", actualTime.toString()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "action", "FAILED",
                "message", "❌ 발송 실패: " + e.getMessage(),
                "simulatedTime", testTime.toString(),
                "actualTime", actualTime.toString()
            ));
        }
    }
    
    /**
     * 정책 체크 (시뮬레이션 시간)
     */
    @Operation(summary = "정책 체크")
    @GetMapping("/check-time")
    public ResponseEntity<Map<String, Object>> checkWithTime(@RequestParam String simulatedTime) {
        LocalTime testTime = LocalTime.parse(simulatedTime);
        LocalTime actualTime = LocalTime.now();
        boolean isBlock = policyService.isBlockTime(testTime);
        
        return ResponseEntity.ok(Map.of(
            "simulatedTime", testTime.toString(),
            "actualTime", actualTime.toString(),
            "isBlockTime", isBlock,
            "message", isBlock ? "⛔ 금지 시간" : "✅ 정상 시간"
        ));
    }
}