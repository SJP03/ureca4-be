package com.ureca.billing.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.Set;

@Tag(name = "Redis Monitor", description = "Redis 중복방지 키 모니터링 API")
@RestController
@RequestMapping("/api/test/redis")
@RequiredArgsConstructor
public class RedisTestController {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * Redis 키 조회
     */
    @Operation(summary = "Redis 키 목록 조회", description = "sent:msg:* 패턴의 중복방지 키 목록")
    @GetMapping("/keys")
    public Map<String, Object> getKeys() {
        Set<String> keys = redisTemplate.keys("sent:msg:EMAIL:*");
        
        return Map.of(
            "totalKeys", keys != null ? keys.size() : 0,
            "keys", keys != null ? keys : Set.of()
        );
    }
    
    /**
     * 특정 키 확인
     */
    @Operation(summary = "특정 billId 중복 체크", description = "해당 billId가 이미 발송되었는지 확인")
    @GetMapping("/check/{billId}")
    public Map<String, Object> checkKey(@PathVariable Long billId) {
        String key = "sent:msg:EMAIL:" + billId;
        Boolean exists = redisTemplate.hasKey(key);
        Long ttl = redisTemplate.getExpire(key);
        
        return Map.of(
            "billId", billId,
            "key", key,
            "exists", exists != null && exists,
            "ttl_seconds", ttl != null ? ttl : -2
        );
    }
    
    /**
     * 모든 키 삭제 (테스트용)
     */
    @Operation(summary = "Redis 키 전체 삭제", description = "중복방지 키 초기화 (테스트용)")
    @DeleteMapping("/clear")
    public Map<String, Object> clearKeys() {
        Set<String> keys = redisTemplate.keys("sent:msg:EMAIL:*");
        
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        
        return Map.of(
            "message", "All keys cleared",
            "deletedCount", keys != null ? keys.size() : 0
        );
    }
}