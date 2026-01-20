package com.ureca.billing.batch.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ureca.billing.core.model.Billing;

import lombok.RequiredArgsConstructor;

/**
 * Billing 데이터를 조회하고 처리하는 배치 서비스
 */
@Service
@RequiredArgsConstructor
public class BillingBatchService {

    private static final Logger log = LoggerFactory.getLogger(BillingBatchService.class);

    private final JdbcTemplate jdbcTemplate;
    private final BillingKafkaProducer kafkaProducer;

    @Value("${billing.batch.chunk-size:1000}")
    private int chunkSize;

    /**
     * 특정 월의 PENDING 상태 billing 데이터를 조회하여 Kafka로 발행합니다.
     *
     * @param yearMonth 처리할 년월 (YYYY-MM 형식)
     * @return 처리된 레코드 수
     */
    @Transactional(readOnly = true)
    public int processBillingByMonth(String yearMonth) {
        log.info("=== Billing 배치 처리 시작: 년월={} ===", yearMonth);

        YearMonth targetMonth = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        int totalProcessed = 0;
        int offset = 0;
        boolean hasMore = true;

        long startTime = System.currentTimeMillis();

        while (hasMore) {
            List<Billing> billings = fetchBillingsByMonth(startDate, endDate, chunkSize, offset);
            
            if (billings.isEmpty()) {
                hasMore = false;
            } else {
                // Kafka로 발행
                kafkaProducer.publishBillings(billings);
                totalProcessed += billings.size();
                offset += billings.size();

                log.info("처리 진행: {}건 발행 완료 (총 {}건)", billings.size(), totalProcessed);

                // 다음 배치가 있는지 확인
                if (billings.size() < chunkSize) {
                    hasMore = false;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("=== Billing 배치 처리 완료: 년월={}, 총 {}건, 소요시간 {}ms ===", 
            yearMonth, totalProcessed, duration);

        return totalProcessed;
    }

    /**
     * 현재 월의 PENDING 상태 billing 데이터를 조회하여 Kafka로 발행합니다.
     *
     * @return 처리된 레코드 수
     */
    @Transactional(readOnly = true)
    public int processCurrentMonthBilling() {
        YearMonth currentMonth = YearMonth.now();
        String yearMonth = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return processBillingByMonth(yearMonth);
    }

    /**
     * 특정 월의 PENDING 상태 billing 데이터를 페이징하여 조회합니다.
     *
     * @param startDate 시작일
     * @param endDate 종료일
     * @param limit 조회할 레코드 수
     * @param offset 오프셋
     * @return Billing 리스트
     */
    private List<Billing> fetchBillingsByMonth(LocalDate startDate, LocalDate endDate, int limit, int offset) {
        String sql = """
            SELECT id, user_id, amount, billing_date, payment_status
            FROM billing
            WHERE billing_date >= ? 
              AND billing_date <= ?
              AND payment_status = 'PENDING'
            ORDER BY id
            LIMIT ? OFFSET ?
            """;

        return jdbcTemplate.query(sql, 
            (rs, rowNum) -> Billing.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .amount(rs.getBigDecimal("amount"))
                .billingDate(rs.getDate("billing_date").toLocalDate())
                .paymentStatus(rs.getString("payment_status"))
                .build(),
            Date.valueOf(startDate),
            Date.valueOf(endDate),
            limit,
            offset
        );
    }

    /**
     * 처리할 년월 목록을 조회합니다 (PENDING 상태 데이터가 있는 월만).
     *
     * @return 년월 리스트 (YYYY-MM 형식)
     */
    public List<String> getAvailableMonths() {
        String sql = """
            SELECT DISTINCT DATE_FORMAT(billing_date, '%Y-%m') as year_month
            FROM billing
            WHERE payment_status = 'PENDING'
            ORDER BY year_month DESC
            """;

        return jdbcTemplate.query(sql, 
            (rs, rowNum) -> rs.getString("year_month")
        );
    }
}
