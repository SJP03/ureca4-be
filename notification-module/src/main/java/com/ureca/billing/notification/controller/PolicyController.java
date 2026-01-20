package com.ureca.billing.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ureca.billing.notification.domain.dto.BlockTimeCheckResponse;
import com.ureca.billing.notification.domain.dto.PolicyResponse;
import com.ureca.billing.notification.service.MessagePolicyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Policy", description = "발송 금지 시간대 정책 API")
@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class PolicyController {
    
    private final MessagePolicyService policyService;
    
    /**
     * EMAIL 정책 조회
     */
    @Operation(summary = "EMAIL 정책 조회", description = "이메일 발송 금지 시간대 설정 조회")
    @GetMapping("/email")
    public ResponseEntity<PolicyResponse> getEmailPolicy() {
        PolicyResponse response = policyService.getPolicyInfo();
        return ResponseEntity.ok(response);
    }
    
    /**
     * 현재 시간 금지 여부 확인
     */
    @Operation(summary = "현재 금지시간 여부 확인", description = "현재 시간이 발송 금지 시간대인지 확인")
    @GetMapping("/check")
    public ResponseEntity<BlockTimeCheckResponse> checkBlockTime() {
        BlockTimeCheckResponse response = policyService.checkBlockTime();
        return ResponseEntity.ok(response);
    }
}