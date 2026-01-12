package com.ureca.billing.notification.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ureca.billing.notification.domain.dto.BillingMessage;
import com.ureca.billing.notification.domain.dto.WaitingQueueStatus;
import com.ureca.billing.notification.service.WaitingQueueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueMonitorController {
    
    private final WaitingQueueService queueService;
    
    /**
     * 테스트용: 메시지 수동 추가
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMessage(@RequestBody BillingMessage message) {
        queueService.addToQueue(message);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Message added to waiting queue",
            "billId", message.getBillId()
        ));
    }
    
    /**
     * 대기열 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<WaitingQueueStatus> getStatus() {
        WaitingQueueStatus status = queueService.getQueueStatus();
        return ResponseEntity.ok(status);
    }
    
    /**
     * 발송 가능한 메시지 조회
     */
    @GetMapping("/ready")
    public ResponseEntity<Set<String>> getReadyMessages(@RequestParam(defaultValue = "10") int limit) {
        Set<String> messages = queueService.getReadyMessages(limit);
        return ResponseEntity.ok(messages);
    }
}