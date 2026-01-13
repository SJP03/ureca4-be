package com.ureca.billing.notification.controller;

import com.ureca.billing.notification.domain.dto.BillingMessage;
import com.ureca.billing.notification.service.EmailService;
import com.ureca.billing.notification.service.MessagePolicyService;
import com.ureca.billing.notification.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    private final MessagePolicyService policyService;
    private final WaitingQueueService queueService;
    private final EmailService emailService;
    
    /**
     * 통합 테스트: 현재 실제 시간으로 발송
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> testSend(@RequestBody BillingMessage message) {
        LocalTime now = LocalTime.now();
        log.info("🧪 Test send request. billId={}, currentTime={}", message.getBillId(), now);
        
        boolean isBlock = policyService.isBlockTime();
        
        if (isBlock) {
            // 금지 시간 → 대기열 저장
            queueService.addToQueue(message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "action", "QUEUED",
                "message", "⏰ 금지 시간입니다. 대기열에 저장되었습니다.",
                "currentTime", now.toString()
            ));
        }
        
        // 정상 시간 → 즉시 발송
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
    @PostMapping("/send-with-time")
    public ResponseEntity<Map<String, Object>> testSendWithTime(
            @RequestBody BillingMessage message,
            @RequestParam String simulatedTime) {  // "23:00" 형식
        
        LocalTime testTime = LocalTime.parse(simulatedTime);
        LocalTime actualTime = LocalTime.now();
        log.info("🧪 Test send with simulated time: {} (actual: {})", testTime, actualTime);
        
        boolean isBlock = policyService.isBlockTime(testTime);  // 테스트 시간으로 체크
        
        if (isBlock) {
            // 금지 시간 → 대기열 저장
            queueService.addToQueue(message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "action", "QUEUED",
                "message", "⏰ 금지 시간입니다. 대기열에 저장되었습니다.",
                "simulatedTime", testTime.toString(),
                "actualTime", actualTime.toString()
            ));
        }
        
        // 정상 시간 → 즉시 발송
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
    @GetMapping("/check-time")
    public ResponseEntity<Map<String, Object>> checkWithTime(
            @RequestParam String simulatedTime) {
        
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
    
    /**
     * 테스트용 메시지 생성
     */
    @GetMapping("/create-message")
    public ResponseEntity<BillingMessage> createTestMessage() {
        BillingMessage message = BillingMessage.builder()
                .billId(1L)
                .userId(1L)
                .billYearMonth("202501")
                .recipientEmail("test@yopmail.com")
                .recipientPhone("01012345678")
                .totalAmount(85000)
                .planFee(46612)
                .addonFee(8500)
                .microPaymentFee(29888)
                .billDate("2025-01-31")
                .dueDate("2025-02-15")
                .planName("5G 프리미어 에센셜")
                .timestamp(LocalTime.now().toString())
                .build();
        
        return ResponseEntity.ok(message);
    }
}