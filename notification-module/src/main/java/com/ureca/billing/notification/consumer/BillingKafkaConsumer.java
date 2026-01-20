package com.ureca.billing.notification.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.billing.core.model.Billing;
import com.ureca.billing.notification.service.EmailNotificationService;

import lombok.RequiredArgsConstructor;

/**
 * Billing Kafka 메시지를 수신하는 Consumer
 */
@Component
@RequiredArgsConstructor
public class BillingKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(BillingKafkaConsumer.class);

    private final ObjectMapper objectMapper;
    private final EmailNotificationService emailNotificationService;

    /**
     * billing-events 토픽에서 메시지를 수신하여 처리합니다.
     *
     * @param message Kafka 메시지 (JSON 문자열)
     * @param key 메시지 키
     * @param acknowledgment 수동 커밋을 위한 Acknowledgment (선택적)
     */
    @KafkaListener(topics = "${billing.kafka.topic:billing-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBillingMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Billing 메시지 수신: key={}, message={}", key, message);
            
            // JSON을 Billing 객체로 역직렬화
            Billing billing = objectMapper.readValue(message, Billing.class);
            
            // 메일 발송 서비스 호출
            emailNotificationService.sendBillingStatement(billing);
            
            // 성공 시 수동 커밋
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("메시지 처리 완료 및 커밋: key={}", key);
            }
            
        } catch (JsonProcessingException e) {
            log.error("Billing 메시지 JSON 역직렬화 실패: key={}, message={}", key, message, e);
            // JSON 파싱 실패는 재시도해도 의미 없으므로 커밋하여 건너뜀
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            log.error("Billing 메시지 처리 중 오류 발생: key={}", key, e);
            // 예외 발생 시 재시도를 위해 커밋하지 않음
            // 재시도 정책은 Spring Kafka의 기본 설정을 따름
            throw e;  // 예외를 다시 던져서 재시도 트리거
        }
    }
}
