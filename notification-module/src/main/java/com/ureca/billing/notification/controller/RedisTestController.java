package com.ureca.billing.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/test/redis")
@RequiredArgsConstructor
public class RedisTestController {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * Redis 키 조회
     */
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