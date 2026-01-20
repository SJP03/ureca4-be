package com.ureca.billing.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.billing.core.dto.BillingMessageDto;
import com.ureca.billing.notification.consumer.handler.DuplicateCheckHandler;
import com.ureca.billing.notification.domain.entity.Notification;
import com.ureca.billing.notification.domain.repository.NotificationRepository;
import com.ureca.billing.notification.service.EmailService;
import com.ureca.billing.notification.service.MessagePolicyService;
import com.ureca.billing.notification.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingEventConsumer {

    private final ObjectMapper objectMapper;
    private final DuplicateCheckHandler duplicateCheckHandler;
    private final MessagePolicyService policyService;
    private final WaitingQueueService queueService;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    @KafkaListener(
        topics = "billing-event",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "3"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String traceInfo = String.format("[P%d-O%d]", record.partition(), record.offset());
        long startTime = System.currentTimeMillis();

        log.info("{} 📥 메시지 수신", traceInfo);

        try {
            // 1. JSON 파싱 (core 모듈의 DTO 사용)
            String messageJson = record.value();
            BillingMessageDto message = objectMapper.readValue(messageJson, BillingMessageDto.class);

            log.info("{} 📨 billId={}, userId={}", traceInfo, message.getBillId(), message.getUserId());

            // 2. 중복 체크
            if (duplicateCheckHandler.isDuplicate(message.getBillId())) {
                log.warn("{} ⚠️ 중복 메시지 스킵. billId={}", traceInfo, message.getBillId());
                ack.acknowledge();
                return;
            }

            // 3. 금지 시간 체크 (22:00 ~ 08:00)
            if (policyService.isBlockTime()) {
                queueService.addToQueue(messageJson);
                saveNotification(message, "PENDING", "Added to waiting queue");
                log.info("{} ⏰ 금지 시간 - 대기열 저장. billId={}", traceInfo, message.getBillId());
                ack.acknowledge();
                return;
            }

            // 4. 이메일 발송
            sendEmail(message, traceInfo);

            // 5. 수동 커밋
            ack.acknowledge();

            long duration = System.currentTimeMillis() - startTime;
            log.info("{} ✅ 처리 완료 ({}ms)", traceInfo, duration);

        } catch (Exception e) {
            log.error("{} ❌ 처리 실패: {}", traceInfo, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void sendEmail(BillingMessageDto message, String traceInfo) {
        try {
            emailService.sendEmail(message);
            duplicateCheckHandler.markAsSent(message.getBillId());
            saveNotification(message, "SENT", null);
            log.info("{} 📧 이메일 발송 성공. billId={}", traceInfo, message.getBillId());

        } catch (Exception e) {
            log.error("{} ❌ 이메일 발송 실패. billId={}", traceInfo, message.getBillId());
            saveNotification(message, "FAILED", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void saveNotification(BillingMessageDto message, String status, String errorMessage) {
        String content = String.format(
            "[LG U+ 청구 알림]\n청구 년월: %s\n총 청구 금액: %,d원\n납부 기한: %s",
            message.getBillYearMonth(),
            message.getTotalAmount() != null ? message.getTotalAmount() : 0,
            message.getDueDate() != null ? message.getDueDate() : "미정"
        );

        Notification notification = Notification.builder()
            .userId(message.getUserId())
            .notificationType("EMAIL")
            .notificationStatus(status)
            .recipient(message.getRecipientEmail())
            .content(content)
            .retryCount(0)
            .scheduledAt(LocalDateTime.now())
            .sentAt("SENT".equals(status) ? LocalDateTime.now() : null)
            .errorMessage(errorMessage)
            .createdAt(LocalDateTime.now())
            .build();

        notificationRepository.save(notification);
    }
}