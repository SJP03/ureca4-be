package com.ureca.billing.notification.handler;

import com.ureca.billing.core.dto.BillingMessageDto;

/**
 * Notification Handler 인터페이스
 * - Strategy 패턴의 핵심
 * - EMAIL, SMS 등 다양한 알림 타입 처리
 */
public interface NotificationHandler {

    /**
     * 알림 처리
     *
     * @param message 청구 메시지
     * @param traceId 추적 ID
     */
    void handle(BillingMessageDto message, String traceId);
    
    String getType();
}