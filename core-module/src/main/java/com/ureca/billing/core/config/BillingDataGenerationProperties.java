package com.ureca.billing.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Billing 데이터 생성 관련 설정 프로퍼티
 */
@Component
@ConfigurationProperties(prefix = "billing.data.generation")
@Getter
@Setter
public class BillingDataGenerationProperties {

    /**
     * 데이터 생성 활성화 여부
     */
    private boolean enabled = false;

    /**
     * 생성할 데이터 건수
     */
    private int count = 100000;
}
