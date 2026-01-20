package com.ureca.billing.core.util;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.ureca.billing.core.model.User;

import net.datafaker.Faker;
import net.datafaker.providers.base.Credentials;
import net.datafaker.providers.base.Number;

/**
 * Users 테이블에 더미 데이터를 생성하는 유틸리티 클래스
 * DataFaker와 JDBC batch insert를 사용하여 대량 데이터를 생성합니다.
 */
@Component
public class UserDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(UserDataGenerator.class);
    private static final int BATCH_SIZE = 1000; // 배치 사이즈
    // private static final int DEFAULT_USER_COUNT = 100000; // 기본 생성할 사용자 수 (10만 명)
    private static final int MIN_AGE = 18; // 최소 나이
    private static final int MAX_AGE = 80; // 최대 나이

    private final JdbcTemplate jdbcTemplate;
    private final Faker faker;
    private final Credentials credentials;
    private final Number number;

    public UserDataGenerator(JdbcTemplate jdbcTemplate, Faker faker) {
        this.jdbcTemplate = jdbcTemplate;
        this.faker = new Faker(new Locale("ko", "KR"));
        this.credentials = this.faker.credentials();
        this.number = this.faker.number();
    }

    /**
     * users 테이블에 더미 데이터를 생성합니다.
     *
     * @param totalCount 생성할 총 데이터 건수 (기본값: 100000)
     */
    public void generateUsersData(int totalCount) {
        log.info("=== Users 데이터 생성 시작: {}건 ===", totalCount);

        int insertedCount = 0;
        int batchCount = 0;
        List<User> batch = new ArrayList<>(BATCH_SIZE);
        Set<String> usedEmails = new HashSet<>();
        Set<String> usedPhones = new HashSet<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalCount; i++) {
            // 고유한 email 생성
            String email = generateUniqueEmail(usedEmails);
            
            // 고유한 phone 생성
            String phone = generateUniquePhone(usedPhones);
            
            // 이름 생성
            String name = generateName();
            
            // 생년월일 생성 (18세 ~ 80세)
            Date birthDate = generateBirthDate();
            
            // 상태는 모두 ACTIVE
            String status = "ACTIVE";

            User user = User.builder()
                    .email(email)
                    .phone(phone)
                    .name(name)
                    .birthDate(birthDate)
                    .status(status)
                    .build();
            
            batch.add(user);

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

        log.info("=== Users 데이터 생성 완료 ===");
        log.info("총 생성 건수: {}건", insertedCount);
        log.info("배치 실행 횟수: {}회", batchCount);
        log.info("소요 시간: {}ms (약 {}초)", duration, duration / 1000.0);
        log.info("평균 처리 속도: {}건/초", insertedCount * 1000 / duration);
    }

    /**
     * 배치 단위로 데이터를 insert합니다.
     * BatchPreparedStatementSetter를 사용하여 타입 안전성과 성능을 모두 확보합니다.
     *
     * @param batch 배치 데이터
     */
    private void insertBatch(List<User> batch) {
        String sql = "INSERT INTO users (email, phone, name, birth_date, status) VALUES (?, ?, ?, ?, ?)";
        
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                User user = batch.get(i);
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getPhone());
                ps.setString(3, user.getName());
                ps.setDate(4, user.getBirthDate());
                ps.setString(5, user.getStatus());
            }

            @Override
            public int getBatchSize() {
                return batch.size();
            }
        });
    }

    /**
     * 고유한 이메일 주소를 생성합니다.
     *
     * @param usedEmails 이미 사용된 이메일 Set
     * @return 고유한 이메일 주소
     */
    private String generateUniqueEmail(Set<String> usedEmails) {
        String email;
        int attempts = 0;
        do {
            // timestamp와 랜덤 숫자를 추가하여 고유성 보장
            email = String.format("%s_%d_%d@%s", 
                credentials.username(),
                System.currentTimeMillis(),
                number.randomNumber(6),
                faker.internet().domainName());
            attempts++;
            if (attempts > 100) {
                // 너무 많은 시도 시 UUID 추가
                email = String.format("%s_%s@%s", 
                    credentials.username(),
                    java.util.UUID.randomUUID().toString().substring(0, 8),
                    faker.internet().domainName());
                break;
            }
        } while (usedEmails.contains(email));
        
        usedEmails.add(email);
        return email;
    }

    /**
     * 고유한 전화번호를 생성합니다 (한국 형식: 010-XXXX-XXXX).
     *
     * @param usedPhones 이미 사용된 전화번호 Set
     * @return 고유한 전화번호
     */
    private String generateUniquePhone(Set<String> usedPhones) {
        String phone;
        int attempts = 0;
        do {
            // 010-XXXX-XXXX 형식
            int middle = number.numberBetween(1000, 10000);
            int last = number.numberBetween(1000, 10000);
            phone = String.format("010-%04d-%04d", middle, last);
            attempts++;
            if (attempts > 100) {
                // 너무 많은 시도 시 timestamp 추가
                long timestamp = System.currentTimeMillis();
                phone = String.format("010-%04d-%04d", 
                    (int)(timestamp % 10000),
                    (int)((timestamp / 10000) % 10000));
                break;
            }
        } while (usedPhones.contains(phone));
        
        usedPhones.add(phone);
        return phone;
    }

    /**
     * 한국 이름을 생성합니다.
     *
     * @return 생성된 이름
     */
    private String generateName() {
        return faker.name().fullName();
    }

    /**
     * 생년월일을 생성합니다 (18세 ~ 80세 범위).
     *
     * @return 생성된 생년월일
     */
    private Date generateBirthDate() {
        LocalDate now = LocalDate.now();
        LocalDate maxDate = now.minusYears(MIN_AGE); // 18세 이상
        LocalDate minDate = now.minusYears(MAX_AGE); // 80세 이하
        
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate);
        int randomDays = number.numberBetween(0, (int) daysBetween);
        LocalDate randomDate = minDate.plusDays(randomDays);
        
        return Date.valueOf(randomDate);
    }
}
