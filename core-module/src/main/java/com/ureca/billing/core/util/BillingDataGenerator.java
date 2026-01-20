package com.ureca.billing.core.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import net.datafaker.Faker;

/**
 * Billing 테이블에 더미 데이터를 생성하는 유틸리티 클래스
 * DataFaker와 JDBC batch insert를 사용하여 대량 데이터를 생성합니다.
 */
@Component
public class BillingDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(BillingDataGenerator.class);
    private static final int BATCH_SIZE = 1000; // 배치 사이즈
    private static final String[] PAYMENT_STATUSES = {"PENDING", "PAID", "FAILED", "CANCELLED"};
    private static final int MIN_AMOUNT = 50000; // 5만원
    private static final int MAX_AMOUNT = 150000; // 15만원
    private static final long MIN_USER_ID = 1L; // 최소 user_id
    private static final long MAX_USER_ID = 100000L; // 최대 user_id (임의 생성 범위)

    private final JdbcTemplate jdbcTemplate;
    private final Faker faker;

    public BillingDataGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.faker = new Faker(new Locale("ko", "KR"));
    }

    /**
     * billing 테이블에 더미 데이터를 생성합니다.
     *
     * @param totalCount 생성할 총 데이터 건수
     */
    public void generateBillingData(int totalCount) {
        log.info("=== Billing 데이터 생성 시작: {}건 ===", totalCount);
        log.info("user_id 생성 범위: {} ~ {}", MIN_USER_ID, MAX_USER_ID);

        int insertedCount = 0;
        int batchCount = 0;
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalCount; i++) {
            // DataFaker를 사용하여 임의의 user_id 생성
            Long userId = generateUserId();

            // 요금 금액 생성 (5만원 ~ 15만원) - DataFaker 사용
            BigDecimal amount = generateAmount();

            // 요금 일자 생성 (최근 1년 내) - DataFaker 사용
            Date billingDate = generateBillingDate();

            // 결제 상태 생성 - DataFaker 사용
            // String paymentStatus = PAYMENT_STATUSES[faker.number().numberBetween(0, PAYMENT_STATUSES.length)];
            String paymentStatus = "PENDING";

            batch.add(new Object[]{userId, amount, billingDate, paymentStatus});

            // 배치 사이즈에 도달하면 일괄 insert
            if (batch.size() >= BATCH_SIZE) {
                insertBatch(batch);
                insertedCount += batch.size();
                batchCount++;
                batch.clear();
                log.info("진행률: {}/{} ({}%)", insertedCount, totalCount, (insertedCount * 100 / totalCount));
            }
        }

        // 남은 데이터 insert
        if (!batch.isEmpty()) {
            insertBatch(batch);
            insertedCount += batch.size();
            batchCount++;
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("=== Billing 데이터 생성 완료 ===");
        log.info("총 생성 건수: {}건", insertedCount);
        log.info("배치 실행 횟수: {}회", batchCount);
        log.info("소요 시간: {}ms (약 {}초)", duration, duration / 1000.0);
        log.info("평균 처리 속도: {}건/초", insertedCount * 1000 / duration);
    }

    /**
     * 배치 단위로 데이터를 insert합니다.
     *
     * @param batch 배치 데이터
     */
    private void insertBatch(List<Object[]> batch) {
        String sql = "INSERT INTO billing (user_id, amount, billing_date, payment_status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, batch);
    }

    /**
     * 요금 금액을 생성합니다 (5만원 ~ 15만원).
     * DataFaker를 사용하여 랜덤 금액을 생성합니다.
     *
     * @return 요금 금액
     */
    private BigDecimal generateAmount() {
        int amount = faker.number().numberBetween(MIN_AMOUNT, MAX_AMOUNT + 1);
        return BigDecimal.valueOf(amount);
    }

    /**
     * 요금 일자를 생성합니다 (최근 1년 내).
     * DataFaker를 사용하여 랜덤 날짜를 생성합니다.
     *
     * @return 요금 일자
     */
    private Date generateBillingDate() {
        LocalDate now = LocalDate.now();
        LocalDate oneYearAgo = now.minusYears(1);
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(oneYearAgo, now);
        int randomDays = faker.number().numberBetween(0, (int) daysBetween);
        LocalDate randomDate = oneYearAgo.plusDays(randomDays);
        return Date.valueOf(randomDate);
    }

    /**
     * 임의의 user_id를 생성합니다.
     * DataFaker를 사용하여 지정된 범위 내의 랜덤 user_id를 생성합니다.
     *
     * @return 생성된 user_id
     */
    private Long generateUserId() {
        int randomUserId = faker.number().numberBetween((int) MIN_USER_ID, (int) MAX_USER_ID + 1);
        return Long.valueOf(randomUserId);
    }
}
