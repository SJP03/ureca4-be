package com.ureca.billing.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ureca.billing.notification.domain.dto.BlockTimeCheckResponse;
import com.ureca.billing.notification.domain.dto.PolicyResponse;
import com.ureca.billing.notification.service.MessagePolicyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class PolicyController {
    
    private final MessagePolicyService policyService;
    
    /**
     * EMAIL 정책 조회
     */
    @GetMapping("/email")
    public ResponseEntity<PolicyResponse> getEmailPolicy() {
        PolicyResponse response = policyService.getPolicyInfo();
        return ResponseEntity.ok(response);
    }
    
    /**
     * 현재 시간 금지 여부 확인
     */
    @GetMapping("/check")
    public ResponseEntity<BlockTimeCheckResponse> checkBlockTime() {
        BlockTimeCheckResponse response = policyService.checkBlockTime();
        return ResponseEntity.ok(response);
    }
}