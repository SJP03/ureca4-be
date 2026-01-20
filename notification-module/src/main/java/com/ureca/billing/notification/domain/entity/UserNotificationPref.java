package com.ureca.billing.notification.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 사용자별 알림 설정 엔티티
 * - 채널별 활성화 여부
 * - 우선순위 (1=primary, 2=fallback)
 * - 개인별 금지 시간대 (quiet_start ~ quiet_end)
 */
@Table("user_notification_prefs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserNotificationPref {
    
    @Id
    @Column("pref_id")
    private Long prefId;
    
    @Column("user_id")
    private Long userId;
    
    @Column("channel")
    private String channel;  // EMAIL, SMS, PUSH
    
    @Column("enabled")
    private Boolean enabled;
    
    @Column("priority")
    private Integer priority;  // 1=primary, 2=fallback
    
    @Column("quiet_start")
    private LocalTime quietStart;  // 금지 시작 시간
    
    @Column("quiet_end")
    private LocalTime quietEnd;    // 금지 종료 시간
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    // ========================================
    // 비즈니스 로직
    // ========================================
    
    /**
     * 현재 시간이 사용자의 금지 시간대인지 확인
     */
    public boolean isQuietTime(LocalTime currentTime) {
        // 금지 시간 미설정 시 false
        if (quietStart == null || quietEnd == null) {
            return false;
        }
        
        // 채널 비활성화 시 항상 금지
        if (!enabled) {
            return true;
        }
        
        // 자정을 넘기는 경우 (예: 22:00 ~ 08:00)
        if (quietStart.isAfter(quietEnd)) {
            return currentTime.isAfter(quietStart) || currentTime.isBefore(quietEnd);
        }
        
        // 일반적인 경우 (예: 09:00 ~ 18:00)
        return currentTime.isAfter(quietStart) && currentTime.isBefore(quietEnd);
    }
    
    /**
     * 금지 시간대 설정 여부 확인
     */
    public boolean hasQuietTime() {
        return quietStart != null && quietEnd != null;
    }
    
    /**
     * 금지 시간대 업데이트 (불변 객체 패턴)
     */
    public UserNotificationPref updateQuietTime(LocalTime newStart, LocalTime newEnd) {
        return UserNotificationPref.builder()
                .prefId(this.prefId)
                .userId(this.userId)
                .channel(this.channel)
                .enabled(this.enabled)
                .priority(this.priority)
                .quietStart(newStart)
                .quietEnd(newEnd)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 채널 활성화/비활성화
     */
    public UserNotificationPref toggleEnabled(boolean enabled) {
        return UserNotificationPref.builder()
                .prefId(this.prefId)
                .userId(this.userId)
                .channel(this.channel)
                .enabled(enabled)
                .priority(this.priority)
                .quietStart(this.quietStart)
                .quietEnd(this.quietEnd)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}