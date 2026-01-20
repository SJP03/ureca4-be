package com.ureca.billing.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * 데이터 생성 관련 설정 프로퍼티
 */
@Component
@ConfigurationProperties(prefix = "billing.data.generation")
@Getter
@Setter
public class DataGenerationProperties {

    // applition.yml 에서 원하는 값으로 설정
    private boolean enabled = true; // 기본값: true
    private int usersCount = 100000; // 기본값: 10만 명
}
