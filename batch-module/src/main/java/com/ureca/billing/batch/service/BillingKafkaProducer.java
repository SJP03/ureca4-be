package com.ureca.billing.batch.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.billing.core.model.Billing;

import lombok.RequiredArgsConstructor;

/**
 * Billing 데이터를 Kafka로 발행하는 Producer 서비스
 */
@Service
@RequiredArgsConstructor
public class BillingKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(BillingKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${billing.kafka.topic:billing-events}")
    private String topicName;

    /**
     * Billing 데이터를 Kafka 토픽으로 발행합니다.
     *
     * @param billing 발행할 Billing 데이터
     */
    public void publishBilling(Billing billing) {
        try {
            String message = objectMapper.writeValueAsString(billing);
            String key = String.valueOf(billing.getId());

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Billing 메시지 발행 성공: billingId={}, offset={}", 
                        billing.getId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Billing 메시지 발행 실패: billingId={}", billing.getId(), ex);
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Billing 객체 JSON 변환 실패: billingId={}", billing.getId(), e);
            throw new RuntimeException("Failed to serialize billing to JSON", e);
        }
    }

    /**
     * 여러 Billing 데이터를 일괄 발행합니다.
     *
     * @param billings 발행할 Billing 데이터 리스트
     */
    public void publishBillings(java.util.List<Billing> billings) {
        for (Billing billing : billings) {
            publishBilling(billing);
        }
    }
}
