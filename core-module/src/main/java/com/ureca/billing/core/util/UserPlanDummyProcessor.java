package com.ureca.billing.core.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.ureca.billing.core.entity.UserPlanStatus;
import com.ureca.billing.core.entity.UserPlans;
import com.ureca.billing.core.entity.Users;

@Component
@StepScope
public class UserPlanDummyProcessor
        implements ItemProcessor<Users, UserPlans>, StepExecutionListener {

    private final JdbcTemplate jdbcTemplate;
    private final List<Long> planIds;
    private final YearMonth targetYm;

    // 이전 월 user_id -> plan_id
    private Map<Long, Long> prevMonthUserPlanMap;
    // 이번 달 이미 생성된 user_id (중복 방지)
    private Set<Long> thisMonthUsers;

    public UserPlanDummyProcessor(
        JdbcTemplate jdbcTemplate,
        @Value("#{jobParameters['targetYearMonth']}") String targetYearMonth
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.planIds = jdbcTemplate.queryForList(
            "SELECT plan_id FROM PLANS WHERE is_active = true",
            Long.class
        );
        this.targetYm = YearMonth.parse(targetYearMonth);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {

        LocalDate thisMonthStart = targetYm.atDay(1);
        LocalDate nextMonthStart = targetYm.plusMonths(1).atDay(1);
        LocalDate prevMonthStart = targetYm.minusMonths(1).atDay(1);

        // ✅ 이전 달 plan preload (user_id -> plan_id)
        prevMonthUserPlanMap = jdbcTemplate.query(
            """
            SELECT user_id, plan_id
            FROM USER_PLANS
            WHERE start_date >= ?
              AND start_date < ?
            """,
            rs -> {
                Map<Long, Long> map = new HashMap<>();
                while (rs.next()) {
                    map.put(
                        rs.getLong("user_id"),
                        rs.getLong("plan_id")
                    );
                }
                return map;
            },
            prevMonthStart,
            thisMonthStart
        );

        // ✅ 이번 달 이미 생성된 user (중복 방지)
        thisMonthUsers = new HashSet<>(jdbcTemplate.queryForList(
            """
            SELECT DISTINCT user_id
            FROM USER_PLANS
            WHERE start_date >= ?
              AND start_date < ?
            """,
            Long.class,
            thisMonthStart,
            nextMonthStart
        ));
    }

    @Override
    public UserPlans process(Users user) {

        Long userId = user.getUserId();

        // 이번 달 이미 있으면 skip
        if (thisMonthUsers.contains(userId)) {
            return null;
        }

        Long planId;
        LocalDate startDate;

        // ✅ 이전 월 유지 가입자
        if (prevMonthUserPlanMap.containsKey(userId)) {
            planId = prevMonthUserPlanMap.get(userId); // 기존 plan 유지
            startDate = targetYm.atDay(1);
        }
        // ✅ 신규 가입자
        else {
            planId = randomPlan();
            int day = ThreadLocalRandom.current()
                .nextInt(1, targetYm.lengthOfMonth() + 1);
            startDate = targetYm.atDay(day);
        }

        return new UserPlans(
            userId,
            planId,
            startDate,
            null,
            UserPlanStatus.ACTIVE
        );
    }

    private Long randomPlan() {
        return planIds.get(
            ThreadLocalRandom.current().nextInt(planIds.size())
        );
    }
}

