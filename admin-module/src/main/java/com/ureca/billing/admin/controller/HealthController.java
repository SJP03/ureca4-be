package com.ureca.billing.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("module", "admin-module");
        response.put("port", "8080");
        response.put("message", "시스템이 정상 작동 중입니다 ✅");
        
        log.info("Health check 요청");
        return response;
    }

    @GetMapping("/db")
    public Map<String, Object> checkDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = jdbcTemplate.queryForObject(
                "SELECT 'MySQL 연결 성공!' as message", 
                String.class
            );
            
            response.put("status", "UP ✅");
            response.put("database", "MySQL");
            response.put("host", "158.179.160.226:3306");
            response.put("dbName", "urecaTeam4_db");
            response.put("message", result);
            
            log.info("✅ DB 연결 테스트 성공");
            
        } catch (Exception e) {
            response.put("status", "DOWN ❌");
            response.put("database", "MySQL");
            response.put("error", e.getMessage());
            log.error("❌ DB 연결 테스트 실패", e);
        }
        return response;
    }

    @GetMapping("/redis")
    public Map<String, Object> checkRedis() {
        Map<String, Object> response = new HashMap<>();
        try {
            String testKey = "health:admin:test";
            String testValue = "Redis 연결 성공!";
            
            redisTemplate.opsForValue().set(testKey, testValue);
            String result = redisTemplate.opsForValue().get(testKey);
            
            response.put("status", "UP ✅");
            response.put("redis", "Connected");
            response.put("host", "158.179.160.226:6379");
            response.put("message", result);
            
            log.info("✅ Redis 연결 테스트 성공");
            
        } catch (Exception e) {
            response.put("status", "DOWN ❌");
            response.put("redis", "Disconnected");
            response.put("error", e.getMessage());
            log.error("❌ Redis 연결 테스트 실패", e);
        }
        return response;
    }

    @GetMapping("/all")
    public Map<String, Object> checkAll() {
        Map<String, Object> response = new HashMap<>();
        
        // Application
        response.put("application", "UP ✅");
        response.put("module", "admin-module");
        response.put("port", "8080");
        
        // Database
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("database", "UP ✅ (158.179.160.226:3306)");
        } catch (Exception e) {
            response.put("database", "DOWN ❌ - " + e.getMessage());
        }
        
        // Redis
        try {
            redisTemplate.opsForValue().set("health:check", "ok");
            response.put("redis", "UP ✅ (158.179.160.226:6379)");
        } catch (Exception e) {
            response.put("redis", "DOWN ❌ - " + e.getMessage());
        }
        
        log.info("전체 헬스체크 완료");
        return response;
    }
}