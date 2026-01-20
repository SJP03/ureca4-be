package com.ureca.billing.batch.job;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ureca.billing.batch.service.BillingBatchService;

import lombok.RequiredArgsConstructor;

/**
 * 월별 Billing 데이터를 처리하는 스케줄 배치 작업
 */
@Component
@RequiredArgsConstructor
public class BillingBatchJob {

    private static final Logger log = LoggerFactory.getLogger(BillingBatchJob.class);

    private final BillingBatchService billingBatchService;

    @Value("${billing.batch.enabled:true}")
    private boolean batchEnabled;

    /**
     * 매월 1일 새벽 2시에 실행되는 배치 작업
     * 전월 데이터를 처리합니다.
     */
    @Scheduled(cron = "${billing.batch.cron:0 0 2 1 * ?}")
    public void processPreviousMonthBilling() {
        if (!batchEnabled) {
            log.info("Billing 배치 작업이 비활성화되어 있습니다.");
            return;
        }

        try {
            // 전월 데이터 처리
            YearMonth previousMonth = YearMonth.now().minusMonths(1);
            String yearMonth = previousMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            log.info("=== 월별 Billing 배치 작업 시작: {} ===", yearMonth);
            
            int processedCount = billingBatchService.processBillingByMonth(yearMonth);
            
            log.info("=== 월별 Billing 배치 작업 완료: {}, 처리 건수: {}건 ===", 
                yearMonth, processedCount);
        } catch (Exception e) {
            log.error("Billing 배치 작업 실행 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 수동으로 특정 월의 데이터를 처리할 수 있는 메서드
     * (테스트 또는 수동 실행용)
     *
     * @param yearMonth 처리할 년월 (YYYY-MM 형식)
     * @return 처리된 레코드 수
     */
    public int processMonthManually(String yearMonth) {
        if (!batchEnabled) {
            log.info("Billing 배치 작업이 비활성화되어 있습니다.");
            return 0;
        }

        try {
            log.info("=== 수동 Billing 배치 작업 시작: {} ===", yearMonth);
            int processedCount = billingBatchService.processBillingByMonth(yearMonth);
            log.info("=== 수동 Billing 배치 작업 완료: {}, 처리 건수: {}건 ===", 
                yearMonth, processedCount);
            return processedCount;
        } catch (Exception e) {
            log.error("수동 Billing 배치 작업 실행 중 오류 발생: {}", yearMonth, e);
            throw e;
        }
    }

    /**
     * 현재 월의 데이터를 처리하는 메서드
     * (테스트 또는 수동 실행용)
     *
     * @return 처리된 레코드 수
     */
    public int processCurrentMonthManually() {
        if (!batchEnabled) {
            log.info("Billing 배치 작업이 비활성화되어 있습니다.");
            return 0;
        }

        try {
            log.info("=== 현재 월 Billing 배치 작업 시작 ===");
            int processedCount = billingBatchService.processCurrentMonthBilling();
            log.info("=== 현재 월 Billing 배치 작업 완료, 처리 건수: {}건 ===", processedCount);
            return processedCount;
        } catch (Exception e) {
            log.error("현재 월 Billing 배치 작업 실행 중 오류 발생", e);
            throw e;
        }
    }
}
