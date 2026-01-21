package com.ureca.billing.notification.config;

import com.ureca.billing.notification.handler.NotificationHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 핸들러 확인
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HandlerInitializer implements ApplicationRunner {
    
    private final NotificationHandlerFactory handlerFactory;
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("=".repeat(60));
        log.info("🚀 Notification Handler 초기화 확인");
        log.info("=".repeat(60));
        
        handlerFactory.printAvailableHandlers();
        
        log.info("=".repeat(60));
    }
}