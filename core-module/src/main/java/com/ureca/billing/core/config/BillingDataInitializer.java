package com.ureca.billing.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import com.ureca.billing.core.util.BillingDataGenerator;

import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션 시작 시 billing 데이터를 자동 생성하는 초기화 클래스
 * application.yml의 billing.data.generation.enabled=true로 설정하면 실행됩니다.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "billing.data.generation.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class BillingDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BillingDataInitializer.class);

    private final BillingDataGenerator billingDataGenerator;
    private final BillingDataGenerationProperties properties;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Billing 데이터 자동 생성 시작 (애플리케이션 시작 시) ===");
        log.info("설정된 데이터 건수: {}건", properties.getCount());
        
        try {
            billingDataGenerator.generateBillingData(properties.getCount());
            log.info("=== Billing 데이터 자동 생성 완료 ===");
        } catch (Exception e) {
            log.error("Billing 데이터 자동 생성 중 오류 발생", e);
            throw e;
        }
    }
}
