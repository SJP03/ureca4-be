package com.ureca.billing.admin.controller;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ureca.billing.admin.dto.BillingBatchResponse;
import com.ureca.billing.batch.service.BillingBatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Billing 배치 정산 로직을 수행하는 관리자 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/billing/batch")
@RequiredArgsConstructor
@Tag(name = "Billing Batch", description = "Billing 배치 정산 API")
public class BillingBatchController {

    private final BillingBatchService billingBatchService;

    /**
     * 특정 월의 billing 데이터를 처리합니다.
     *
     * @param yearMonth 처리할 년월 (YYYY-MM 형식)
     * @return 배치 처리 결과
     */
    @PostMapping("/process/{yearMonth}")
    @Operation(summary = "특정 월 배치 처리", description = "지정된 년월의 PENDING 상태 billing 데이터를 Kafka로 발행합니다.")
    public ResponseEntity<BillingBatchResponse> processBillingByMonth(
            @Parameter(description = "처리할 년월 (예: 2024-01)", required = true)
            @PathVariable String yearMonth) {
        
        log.info("=== Billing 배치 처리 요청: 년월={} ===", yearMonth);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 년월 형식 검증
            YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
            
            int processedCount = billingBatchService.processBillingByMonth(yearMonth);
            
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            
            BillingBatchResponse response = BillingBatchResponse.builder()
                    .yearMonth(yearMonth)
                    .processedCount(processedCount)
                    .status("SUCCESS")
                    .startTime(startTime)
                    .endTime(endTime)
                    .durationMs(durationMs)
                    .build();
            
            log.info("=== Billing 배치 처리 완료: 년월={}, 처리건수={}건, 소요시간={}ms ===", 
                yearMonth, processedCount, durationMs);
            
            return ResponseEntity.ok(response);
            
        } catch (java.time.format.DateTimeParseException e) {
            log.error("잘못된 년월 형식: {}", yearMonth, e);
            BillingBatchResponse response = BillingBatchResponse.builder()
                    .yearMonth(yearMonth)
                    .processedCount(0)
                    .status("FAILED")
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .errorMessage("잘못된 년월 형식입니다. YYYY-MM 형식으로 입력해주세요.")
                    .build();
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Billing 배치 처리 중 오류 발생: 년월={}", yearMonth, e);
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            
            BillingBatchResponse response = BillingBatchResponse.builder()
                    .yearMonth(yearMonth)
                    .processedCount(0)
                    .status("FAILED")
                    .startTime(startTime)
                    .endTime(endTime)
                    .durationMs(durationMs)
                    .errorMessage(e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 현재 월의 billing 데이터를 처리합니다.
     *
     * @return 배치 처리 결과
     */
    @PostMapping("/process/current")
    @Operation(summary = "현재 월 배치 처리", description = "현재 월의 PENDING 상태 billing 데이터를 Kafka로 발행합니다.")
    public ResponseEntity<BillingBatchResponse> processCurrentMonthBilling() {
        YearMonth currentMonth = YearMonth.now();
        String yearMonth = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        log.info("=== 현재 월 Billing 배치 처리 요청: 년월={} ===", yearMonth);
        
        return processBillingByMonth(yearMonth);
    }

    /**
     * 전월의 billing 데이터를 처리합니다.
     *
     * @return 배치 처리 결과
     */
    @PostMapping("/process/previous")
    @Operation(summary = "전월 배치 처리", description = "전월의 PENDING 상태 billing 데이터를 Kafka로 발행합니다.")
    public ResponseEntity<BillingBatchResponse> processPreviousMonthBilling() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        String yearMonth = previousMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        log.info("=== 전월 Billing 배치 처리 요청: 년월={} ===", yearMonth);
        
        return processBillingByMonth(yearMonth);
    }

    /**
     * 처리 가능한 월 목록을 조회합니다.
     *
     * @return 처리 가능한 년월 목록
     */
    @GetMapping("/months")
    @Operation(summary = "처리 가능한 월 목록 조회", description = "PENDING 상태 billing 데이터가 있는 년월 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getAvailableMonths() {
        log.info("=== 처리 가능한 월 목록 조회 요청 ===");
        
        try {
            List<String> availableMonths = billingBatchService.getAvailableMonths();
            
            Map<String, Object> response = new HashMap<>();
            response.put("availableMonths", availableMonths);
            response.put("count", availableMonths.size());
            response.put("timestamp", LocalDateTime.now());
            
            log.info("=== 처리 가능한 월 목록 조회 완료: {}개 ===", availableMonths.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("처리 가능한 월 목록 조회 중 오류 발생", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("availableMonths", List.of());
            response.put("count", 0);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
