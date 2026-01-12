package com.ureca.billing.notification.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;

import java.time.LocalDateTime;

@Table("notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @Column("notification_id")
    private Long notificationId;
    
    @Column("user_id")
    private Long userId;
    
    @Column("bill_id")
    private Long billId;
    
    @Column("notification_type")
    private String notificationType;  // "EMAIL" or "SMS"
    
    @Column("notification_status")
    private String notificationStatus;  // "PENDING", "WAITING", "SENT", "FAILED", "RETRY"
    
    @Column("recipient")
    private String recipient;
    
    @Column("content")
    private String content;
    
    @Column("retry_count")
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column("scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column("sent_at")
    private LocalDateTime sentAt;
    
    @Column("error_message")
    private String errorMessage;
    
    @Column("created_at")
    private LocalDateTime createdAt;
}