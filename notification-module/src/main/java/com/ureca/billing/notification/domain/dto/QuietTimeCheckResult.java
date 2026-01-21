package com.ureca.billing.notification.domain.dto;

import lombok.*;

import java.time.LocalTime;

/**
 * 금지 시간 체크 결과 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuietTimeCheckResult {
    
    private boolean isQuietTime;      // 금지 시간 여부
    private String reason;            // 사유: ALLOWED, USER_QUIET_TIME, SYSTEM_POLICY, CHANNEL_DISABLED
    private String source;            // 출처: USER_PREF, SYSTEM_POLICY
    private Long userId;
    private String channel;
    private LocalTime checkTime;      // 체크한 시간
    private LocalTime quietStart;     // 금지 시작
    private LocalTime quietEnd;       // 금지 종료
    private String message;           // 사용자용 메시지
    
    /**
     * 발송 가능 여부 (isQuietTime의 반대)
     */
    public boolean canSend() {
        return !isQuietTime;
    }
    
    /**
     * 금지 시간대 문자열 (예: "22:00 ~ 08:00")
     */
    public String getQuietPeriod() {
        if (quietStart == null || quietEnd == null) {
            return "N/A";
        }
        return quietStart + " ~ " + quietEnd;
    }
}