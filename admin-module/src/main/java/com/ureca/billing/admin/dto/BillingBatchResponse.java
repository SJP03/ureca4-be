package com.ureca.billing.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Billing 배치 처리 결과 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingBatchResponse {
    
    /**
     * 처리된 년월 (YYYY-MM 형식)
     */
    private String yearMonth;
    
    /**
     * 처리된 레코드 수
     */
    private int processedCount;
    
    /**
     * 처리 상태 (SUCCESS, FAILED)
     */
    private String status;
    
    /**
     * 처리 시작 시간
     */
    private LocalDateTime startTime;
    
    /**
     * 처리 완료 시간
     */
    private LocalDateTime endTime;
    
    /**
     * 소요 시간 (밀리초)
     */
    private Long durationMs;
    
    /**
     * 에러 메시지 (실패 시)
     */
    private String errorMessage;
}
