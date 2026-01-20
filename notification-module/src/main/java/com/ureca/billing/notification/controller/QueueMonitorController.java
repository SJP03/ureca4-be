package com.ureca.billing.notification.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.billing.core.dto.BillingMessageDto;
import com.ureca.billing.notification.domain.dto.WaitingQueueStatus;
import com.ureca.billing.notification.service.WaitingQueueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Queue Monitor", description = "대기열(금지시간) 모니터링 API")
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
@Slf4j
public class QueueMonitorController {
    
    private final WaitingQueueService queueService;
    private final ObjectMapper objectMapper;
    
    /**
     * 테스트용: 메시지 수동 추가
     */
    @Operation(summary = "대기열에 메시지 수동 추가", description = "테스트용 메시지를 대기열에 추가")
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMessage(@RequestBody BillingMessageDto message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            queueService.addToQueue(messageJson);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message added to waiting queue",
                "billId", message.getBillId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * 대기열 상태 조회
     */
    @Operation(summary = "대기열 상태 조회", description = "대기열 크기 및 발송 대기 메시지 수")
    @GetMapping("/status")
    public ResponseEntity<WaitingQueueStatus> getStatus() {
        WaitingQueueStatus status = queueService.getQueueStatus();
        return ResponseEntity.ok(status);
    }
    
    /**
     * 발송 가능한 메시지 조회
     */
    @Operation(summary = "발송 가능 메시지 조회", description = "금지시간 해제 후 발송 가능한 메시지 목록")
    @GetMapping("/ready")
    public ResponseEntity<Set<String>> getReadyMessages(@RequestParam(defaultValue = "10") int limit) {
        Set<String> messages = queueService.getReadyMessages(limit);
        return ResponseEntity.ok(messages);
    }
}