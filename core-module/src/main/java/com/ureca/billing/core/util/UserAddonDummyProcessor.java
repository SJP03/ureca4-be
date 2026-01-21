package com.ureca.billing.core.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

import com.ureca.billing.core.entity.AddonStatus;
import com.ureca.billing.core.entity.UserAddons;

@Component
@StepScope
public class UserAddonDummyProcessor implements ItemProcessor<Long, UserAddons>, StepExecutionListener {

    private final JdbcTemplate jdbcTemplate;
    private final YearMonth targetYm;
    private final List<Long> userIds;
    private final List<Long> addonIds;

    // 이전달 end_date가 null인 데이터 유지용
    private final List<UserAddons> carryOverAddons = new ArrayList<>();

    // 생성 중 중복 방지
    private final Map<Long, Set<Long>> assignedAddons = new HashMap<>();

    public UserAddonDummyProcessor(
            JdbcTemplate jdbcTemplate,
            @Value("#{jobParameters['targetYearMonth']}") String targetYearMonth
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.targetYm = YearMonth.parse(targetYearMonth);
        this.userIds = jdbcTemplate.queryForList("SELECT user_id FROM USERS", Long.class);
        this.addonIds = jdbcTemplate.queryForList("SELECT addon_id FROM ADDONS WHERE is_active = true", Long.class);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {

        // 이전달 데이터 가져오기 (end_date가 null인 것만 유지)
        jdbcTemplate.query(
                """
                SELECT user_id, addon_id
                FROM USER_ADDONS
                WHERE end_date IS NULL
                """,
                (RowCallbackHandler) rs -> {
                    Long userId = rs.getLong("user_id");
                    Long addonId = rs.getLong("addon_id");

                    carryOverAddons.add(new UserAddons(
                            userId,
                            addonId,
                            targetYm.atDay(1), // 항상 이번달 1일
                            null,
                            AddonStatus.ACTIVE
                    ));

                    assignedAddons.computeIfAbsent(userId, k -> new HashSet<>()).add(addonId);
                }
        );
    }

    @Override
    public UserAddons process(Long seq) {

        // 1️⃣ 이전달 carryOver 데이터 먼저 반환
        if (!carryOverAddons.isEmpty()) {
            UserAddons ua = carryOverAddons.remove(0);

            // 일정 비율로 캔슬 처리
            if (ThreadLocalRandom.current().nextInt(100) < 20) { // 20% 캔슬
                LocalDate endDate = randomDayInMonth();
                ua.setEndDate(endDate);
                ua.setStatus(AddonStatus.CANCELLED);
            }

            return ua;
        }

        // 2️⃣ 신규 생성
        Long userId = randomUserId();
        Set<Long> userAddonSet = assignedAddons.computeIfAbsent(userId, k -> new HashSet<>());

        if (userAddonSet.size() >= addonIds.size()) return null; // 모든 addon 이미 있음

        Long addonId;
        do {
            addonId = randomAddonId();
        } while (userAddonSet.contains(addonId));
        userAddonSet.add(addonId);

        LocalDate startDate = randomDayInMonth();
        AddonStatus status = AddonStatus.ACTIVE;
        LocalDate endDate = null;

        // 일정 비율로 캔슬 처리
        if (ThreadLocalRandom.current().nextInt(100) < 20) { // 20% 캔슬
            endDate = randomDayInMonth();
            status = AddonStatus.CANCELLED;
        }

        return new UserAddons(userId, addonId, startDate, endDate, status);
    }

    private Long randomUserId() {
        return userIds.get(ThreadLocalRandom.current().nextInt(userIds.size()));
    }

    private Long randomAddonId() {
        return addonIds.get(ThreadLocalRandom.current().nextInt(addonIds.size()));
    }

    private LocalDate randomDayInMonth() {
        return targetYm.atDay(ThreadLocalRandom.current().nextInt(1, targetYm.lengthOfMonth() + 1));
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
