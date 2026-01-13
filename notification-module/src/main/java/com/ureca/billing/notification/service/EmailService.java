package com.ureca.billing.notification.service;

import com.ureca.billing.notification.domain.dto.BillingMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class EmailService {
    
    private final Random random = new Random();
    
    /**
     * 이메일 발송 (Mocking)
     * - 1초 지연
     * - 1% 확률로 실패
     */
    public void sendEmail(BillingMessage message) throws Exception {
        log.info("📧 Sending email to: {} (billId={})", 
                message.getRecipientEmail(), message.getBillId());
        
        // 1초 지연 (네트워크 지연 시뮬레이션)
        Thread.sleep(1000);
        
        // 1% 확률로 실패
        if (random.nextInt(100) < 1) {
            log.error("❌ Email send failed (1% probability). billId={}", message.getBillId());
            throw new RuntimeException("Email send failed (SMTP error simulation)");
        }
        
        log.info("✅ Email sent successfully. billId={}, amount={}", 
                message.getBillId(), message.getTotalAmount());
    }
}