package com.ureca.billing.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public String hello() {
        log.info("✅ Hello World API 호출됨");
        return "Hello, LG U+ Billing System! 🎉";
    }

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "LG U+ 청구서 시스템 멀티 모듈 프로젝트");
        response.put("module", "admin-module");
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        
        log.info("✅ Test API 호출됨: {}", response);
        return response;
    }
}