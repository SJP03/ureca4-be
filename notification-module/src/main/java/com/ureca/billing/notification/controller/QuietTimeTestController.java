package com.ureca.billing.notification.controller;

import com.ureca.billing.core.dto.BillingMessageDto;  // ✅ core-module의 DTO 사용
import com.ureca.billing.notification.domain.dto.QuietTimeCheckResult;
import com.ureca.billing.notification.service.EmailService;
import com.ureca.billing.notification.service.UserQuietTimeService;
import com.ureca.billing.notification.service.WaitingQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Quiet Time Test", description = "사용자별 금지 시간대 테스트 API")
@RestController
@RequestMapping("/api/test/quiet-time")
@RequiredArgsConstructor
@Slf4j
public class QuietTimeTestController {
    
    private final UserQuietTimeService quietTimeService;
    private final WaitingQueueService queueService;
    private final EmailService emailService;
    
    // ========================================
    // 금지 시간 체크 테스트
    // ========================================
    
    @Operation(summary = "현재 시간으로 금지 시간 체크")
    @GetMapping("/check/{userId}")
    public ResponseEntity<QuietTimeCheckResult> checkNow(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "EMAIL") String channel) {
        
        QuietTimeCheckResult result = quietTimeService.checkQuietTime(userId, channel);
        return ResponseEntity.ok(result);
    }
    
    @Operation(summary = "특정 시간으로 금지 시간 체크 (시뮬레이션)")
    @GetMapping("/simulate/{userId}")
    public ResponseEntity<Map<String, Object>> simulateCheck(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "EMAIL") String channel,
            @RequestParam String time) {
        
        LocalTime checkTime = LocalTime.parse(time);
        QuietTimeCheckResult result = quietTimeService.checkQuietTime(userId, channel, checkTime);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("channel", channel);
        response.put("simulatedTime", time);
        response.put("actualTime", LocalTime.now().toString());
        response.put("checkResult", result);
        
        return ResponseEntity.ok(response);
    }
    
   
    // ========================================
    // 통합 발송 테스트
    // ========================================
    
    @Operation(summary = "사용자별 금지 시간 적용 발송 테스트")
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> testSend(@RequestBody BillingMessageDto message) {
        LocalTime now = LocalTime.now();
        log.info("🧪 Test send with user quiet time. userId={}, billId={}, time={}", 
                message.getUserId(), message.getBillId(), now);
        
        QuietTimeCheckResult quietCheck = quietTimeService.checkQuietTime(
                message.getUserId(), "EMAIL");
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", message.getUserId());
        response.put("billId", message.getBillId());
        response.put("currentTime", now.toString());
        response.put("quietCheck", quietCheck);
        
        if (quietCheck.isQuietTime()) {
            queueService.addToQueue(message);
            
            response.put("action", "QUEUED");
            response.put("message", String.format("⏰ 금지 시간입니다 (%s). 대기열에 저장되었습니다.", 
                    quietCheck.getReason()));
            
        } else {
            try {
                emailService.sendEmail(message);
                response.put("action", "SENT");
                response.put("message", "✅ 이메일이 즉시 발송되었습니다.");
            } catch (Exception e) {
                response.put("action", "FAILED");
                response.put("message", "❌ 발송 실패: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "시뮬레이션 시간으로 발송 테스트")
    @PostMapping("/send-at")
    public ResponseEntity<Map<String, Object>> testSendAt(
            @RequestBody BillingMessageDto message,
            @RequestParam String simulatedTime) {
        
        LocalTime checkTime = LocalTime.parse(simulatedTime);
        LocalTime actualTime = LocalTime.now();
        
        log.info("🧪 Test send with simulated time. userId={}, simTime={}, actualTime={}", 
                message.getUserId(), checkTime, actualTime);
        
        QuietTimeCheckResult quietCheck = quietTimeService.checkQuietTime(
                message.getUserId(), "EMAIL", checkTime);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", message.getUserId());
        response.put("billId", message.getBillId());
        response.put("simulatedTime", simulatedTime);
        response.put("actualTime", actualTime.toString());
        response.put("quietCheck", quietCheck);
        
        if (quietCheck.isQuietTime()) {
            response.put("action", "WOULD_BE_QUEUED");
            response.put("message", String.format("⏰ 해당 시간은 금지 시간입니다 (%s)", 
                    quietCheck.getReason()));
        } else {
            response.put("action", "WOULD_BE_SENT");
            response.put("message", "✅ 해당 시간은 발송 가능합니다");
        }
        
        return ResponseEntity.ok(response);
    }
    
}