package com.ureca.billing.notification.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ureca.billing.core.model.Billing;

import lombok.RequiredArgsConstructor;

/**
 * 이메일 알림 발송 서비스
 * 실제 메일 발송 없이 로그만 출력합니다.
 */
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    /**
     * 요금 명세서를 이메일로 전송합니다.
     * 실제 메일 발송 없이 로그만 출력합니다.
     *
     * @param billing 전송할 Billing 정보
     */
    public void sendBillingStatement(Billing billing) {
        Long userId = billing.getUserId();
        BigDecimal amount = billing.getAmount();
        LocalDate billingDate = billing.getBillingDate();

        log.info("user_id {}에게 {} 기준 {}원 요금 명세서를 전송했습니다.", 
            userId, billingDate, amount);
    }
}
