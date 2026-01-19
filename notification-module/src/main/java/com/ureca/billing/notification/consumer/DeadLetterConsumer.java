package com.ureca.billing.notification.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.billing.notification.domain.dto.BillingMessage;
import com.ureca.billing.notification.domain.entity.Notification;
import com.ureca.billing.notification.domain.repository.NotificationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterConsumer {
	
	private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // billing-event.DLT (죽은 편지함)만 감시하는 녀석
    @KafkaListener(topics = "billing-event.DLT", groupId = "dlq-group")
    public void listenDeadLetter(String messageJson) {
        // 여기서는 에러 없이 로그만 찍거나, DB에 '실패_목록'으로 저장합니다.
    	try {
            BillingMessage message = objectMapper.readValue(messageJson, BillingMessage.class);
            
            // DB에 실패 이력 저장
            saveFailedNotification(message);
            
            log.error("🚑 [DLT] 저장 완료. billId={}", message.getBillId());
            
        } catch (Exception e) {
            log.error("❌ [DLT] 처리 실패: {}", e.getMessage());
        }
    }
    
    private void saveFailedNotification(BillingMessage message) {
        Notification notification = Notification.builder()
            .userId(message.getUserId())
            .notificationType("EMAIL")
            .notificationStatus("FAILED")
            .recipient(message.getRecipientEmail())
            .retryCount(3) // 이미 3회 재시도 완료
            .errorMessage("Moved to DLT after 3 retries")
            .createdAt(LocalDateTime.now())
            .build();
        
        notificationRepository.save(notification);
    }
}