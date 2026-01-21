package com.ureca.billing.notification.domain.dto;

import lombok.*;

import java.time.LocalTime;

/**
 * 사용자 알림 설정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrefRequest {
    
    private Long userId;
    private String channel;       // EMAIL, SMS, PUSH
    private Boolean enabled;      // 채널 활성화 여부
    private Integer priority;     // 우선순위 (1=primary, 2=fallback)
    private LocalTime quietStart; // 금지 시작 시간
    private LocalTime quietEnd;   // 금지 종료 시간
    
    /**
     * 금지 시간대만 설정하는 간편 생성자
     */
    public static UserPrefRequest ofQuietTime(Long userId, String channel, 
                                               LocalTime quietStart, LocalTime quietEnd) {
        return UserPrefRequest.builder()
                .userId(userId)
                .channel(channel)
                .quietStart(quietStart)
                .quietEnd(quietEnd)
                .build();
    }
    
    /**
     * 채널 토글용 간편 생성자
     */
    public static UserPrefRequest ofToggle(Long userId, String channel, boolean enabled) {
        return UserPrefRequest.builder()
                .userId(userId)
                .channel(channel)
                .enabled(enabled)
                .build();
    }
}