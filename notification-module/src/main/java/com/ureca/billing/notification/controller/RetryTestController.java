package com.ureca.billing.notification.controller;

import com.ureca.billing.notification.domain.repository.NotificationRepository;
import com.ureca.billing.notification.service.RetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "DLT Retry Test", description = "DLT 재시도 로직 테스트 API")
@RestController
@RequestMapping("/api/test/retry")
@RequiredArgsConstructor
public class RetryTestController {
    
    private final RetryService retryService;
    private final NotificationRepository notificationRepository;
    
    @Operation(summary = "재시도 스케줄러 수동 실행")
    @PostMapping("/run")
    public Map<String, Object> runRetryManually() {
        int retryCount = retryService.retryFailedMessages(100);
        
        return Map.of(
            "success", true,
            "retriedCount", retryCount,
            "message", "Retry process completed"
        );
    }
    
    @Operation(summary = "FAILED 상태 메시지 개수 조회")
    @GetMapping("/failed-count")
    public Map<String, Object> getFailedCount() {
        long failedCount = notificationRepository
            .findFailedMessagesForRetry()
            .size();
        
        return Map.of(
            "failedCount", failedCount,
            "message", "FAILED 상태이면서 재시도 가능한 메시지 수"
        );
    }
    
    @Operation(summary = "모든 Notification 상태 요약")
    @GetMapping("/status-summary")
    public Map<String, Object> getStatusSummary() {
        Map<String, Long> summary = new HashMap<>();
        
        // 전체 조회 후 상태별 카운트 (간단한 구현)
        var allNotifications = notificationRepository.findAll();
        
        long sentCount = 0;
        long failedCount = 0;
        long retryCount = 0;
        long pendingCount = 0;
        
        for (var notification : allNotifications) {
            switch (notification.getNotificationStatus()) {
                case "SENT" -> sentCount++;
                case "FAILED" -> failedCount++;
                case "RETRY" -> retryCount++;
                case "PENDING" -> pendingCount++;
            }
        }
        
        return Map.of(
            "SENT", sentCount,
            "FAILED", failedCount,
            "RETRY", retryCount,
            "PENDING", pendingCount,
            "total", sentCount + failedCount + retryCount + pendingCount
        );
    }
}