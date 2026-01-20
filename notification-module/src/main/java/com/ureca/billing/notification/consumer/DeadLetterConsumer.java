package com.ureca.billing.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.billing.core.dto.BillingMessageDto;
import com.ureca.billing.notification.domain.entity.Notification;
import com.ureca.billing.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterConsumer {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "billing-event.DLT",
        groupId = "dlq-group",
        concurrency = "3"
    )
    public void listenDeadLetter(ConsumerRecord<String, String> record) {
        String traceInfo = String.format("[DLT-P%d-O%d]", record.partition(), record.offset());
        log.error("{} 🚨 DLT 메시지 수신", traceInfo);

        try {
            String messageJson = record.value();

            // ✅ 이중 직렬화 처리 (기존 잘못된 메시지 호환)
            if (messageJson.startsWith("\"") && messageJson.endsWith("\"")) {
                messageJson = objectMapper.readValue(messageJson, String.class);
            }

            BillingMessageDto message = objectMapper.readValue(messageJson, BillingMessageDto.class);

            // DB에 실패 이력 저장
            saveFailedNotification(message);

            log.error("{} 🚑 DLT 저장 완료. billId={}", traceInfo, message.getBillId());

        } catch (Exception e) {
            log.error("{} ❌ DLT 처리 실패: {}", traceInfo, e.getMessage());
        }
    }

    private void saveFailedNotification(BillingMessageDto message) {
        String content = String.format(
            "[LG U+ 청구 알림 - 최종 실패]\n청구 년월: %s\n총 청구 금액: %,d원",
            message.getBillYearMonth(),
            message.getTotalAmount()
        );

        Notification notification = Notification.builder()
            .userId(message.getUserId())
            .notificationType("EMAIL")
            .notificationStatus("FAILED")
            .recipient(message.getRecipientEmail())
            .content(content)
            .retryCount(3)
            .errorMessage("Moved to DLT after 3 retries")
            .createdAt(LocalDateTime.now())
            .build();

        notificationRepository.save(notification);
    }
}