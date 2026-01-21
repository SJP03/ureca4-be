package com.ureca.billing.notification.domain.dto;

import com.ureca.billing.notification.domain.entity.UserNotificationPref;
import lombok.*;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 사용자 알림 설정 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrefResponse {
    
    private Long prefId;
    private Long userId;
    private String channel;
    private Boolean enabled;
    private Integer priority;
    private String quietStart;       // "22:00:00" 형식
    private String quietEnd;         // "08:00:00" 형식
    private String quietPeriod;      // "22:00 ~ 08:00" 형식
    private Boolean hasQuietTime;    // 금지 시간 설정 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Entity → DTO 변환
     */
    public static UserPrefResponse from(UserNotificationPref entity) {
        String quietPeriod = null;
        if (entity.getQuietStart() != null && entity.getQuietEnd() != null) {
            quietPeriod = formatTime(entity.getQuietStart()) + " ~ " + formatTime(entity.getQuietEnd());
        }
        
        return UserPrefResponse.builder()
                .prefId(entity.getPrefId())
                .userId(entity.getUserId())
                .channel(entity.getChannel())
                .enabled(entity.getEnabled())
                .priority(entity.getPriority())
                .quietStart(entity.getQuietStart() != null ? entity.getQuietStart().toString() : null)
                .quietEnd(entity.getQuietEnd() != null ? entity.getQuietEnd().toString() : null)
                .quietPeriod(quietPeriod)
                .hasQuietTime(entity.hasQuietTime())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    private static String formatTime(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}