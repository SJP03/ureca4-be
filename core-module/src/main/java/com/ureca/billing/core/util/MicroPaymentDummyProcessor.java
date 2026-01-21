package com.ureca.billing.core.util;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.ureca.billing.core.entity.MicroPayments;
import com.ureca.billing.core.entity.PaymentType;

@Component
@StepScope
public class MicroPaymentDummyProcessor implements ItemProcessor<Long, MicroPayments> {

    private final List<Long> userIds;
    private final Random random = new Random();
    private final YearMonth targetYm;

    public MicroPaymentDummyProcessor(
        JdbcTemplate jdbcTemplate,
        @Value("#{jobParameters['targetYearMonth']}") String targetYearMonth
    ) {
        this.userIds = jdbcTemplate.queryForList(
            "SELECT user_id FROM USERS",
            Long.class
        );
        this.targetYm = YearMonth.parse(targetYearMonth);
    }

    @Override
    public MicroPayments process(Long seq) {
        Long userId = userIds.get(ThreadLocalRandom.current().nextInt(userIds.size()));
        int amount = 100 + random.nextInt(9901);
        String merchantName = "Merchant_" + (1 + random.nextInt(100));
        PaymentType type = randomEnum(PaymentType.class);

        // paymentDate를 targetYearMonth 안에서 랜덤하게 설정
        LocalDateTime paymentDate = targetYm.atDay(
            ThreadLocalRandom.current().nextInt(1, targetYm.lengthOfMonth() + 1)
        ).atTime(
            ThreadLocalRandom.current().nextInt(0, 24),
            ThreadLocalRandom.current().nextInt(0, 60),
            ThreadLocalRandom.current().nextInt(0, 60)
        );

        return new MicroPayments(userId, amount, merchantName, type, paymentDate);
    }

    private <T extends Enum<?>> T randomEnum(Class<T> clazz) {
        T[] values = clazz.getEnumConstants();
        return values[random.nextInt(values.length)];
    }
}


