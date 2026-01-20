package com.ureca.billing.core.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Billing 엔티티 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Billing {
    
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private LocalDate billingDate;
    private String paymentStatus;
}
