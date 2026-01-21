package com.ureca.billing.notification.service;

import com.ureca.billing.notification.domain.dto.QuietTimeCheckResult;
import com.ureca.billing.notification.domain.dto.UserPrefRequest;
import com.ureca.billing.notification.domain.dto.UserPrefResponse;
import com.ureca.billing.notification.domain.entity.UserNotificationPref;
import com.ureca.billing.notification.domain.repository.UserNotificationPrefRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자별 알림 설정 및 금지 시간대 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserQuietTimeService {
    
    private final UserNotificationPrefRepository prefRepository;
    private final MessagePolicyService systemPolicyService;  // 시스템 정책
    
    // ========================================
    // 금지 시간 체크 (핵심 로직)
    // ========================================
    
    /**
     * 사용자별 + 시스템 금지 시간 통합 체크
     * 
     * 우선순위:
     * 1. 사용자 설정이 있으면 사용자 설정 적용
     * 2. 사용자 설정이 없으면 시스템 정책 적용
     * 
     * @param userId 사용자 ID
     * @param channel 채널 (EMAIL, SMS, PUSH)
     * @return QuietTimeCheckResult 금지 시간 체크 결과
     */
    public QuietTimeCheckResult checkQuietTime(Long userId, String channel) {
        LocalTime now = LocalTime.now();
        return checkQuietTime(userId, channel, now);
    }
    
    /**
     * 특정 시간에 대한 금지 시간 체크 (테스트용)
     */
    public QuietTimeCheckResult checkQuietTime(Long userId, String channel, LocalTime checkTime) {
        log.debug("🔍 Checking quiet time. userId={}, channel={}, time={}", userId, channel, checkTime);
        
        // 1. 사용자 설정 조회
        Optional<UserNotificationPref> userPref = getUserPref(userId, channel);
        
        if (userPref.isPresent()) {
            UserNotificationPref pref = userPref.get();
            
            // 채널 비활성화 시
            if (!pref.getEnabled()) {
                log.info("🚫 Channel disabled for user. userId={}, channel={}", userId, channel);
                return QuietTimeCheckResult.builder()
                        .isQuietTime(true)
                        .reason("CHANNEL_DISABLED")
                        .source("USER_PREF")
                        .userId(userId)
                        .channel(channel)
                        .checkTime(checkTime)
                        .quietStart(null)
                        .quietEnd(null)
                        .message("사용자가 해당 채널을 비활성화했습니다.")
                        .build();
            }
            
            // 사용자 금지 시간대 체크
            if (pref.hasQuietTime()) {
                boolean isQuiet = pref.isQuietTime(checkTime);
                
                log.info("⏰ User quiet time check. userId={}, isQuiet={}, quietTime={}-{}", 
                        userId, isQuiet, pref.getQuietStart(), pref.getQuietEnd());
                
                return QuietTimeCheckResult.builder()
                        .isQuietTime(isQuiet)
                        .reason(isQuiet ? "USER_QUIET_TIME" : "ALLOWED")
                        .source("USER_PREF")
                        .userId(userId)
                        .channel(channel)
                        .checkTime(checkTime)
                        .quietStart(pref.getQuietStart())
                        .quietEnd(pref.getQuietEnd())
                        .message(isQuiet 
                                ? String.format("사용자 금지 시간대 (%s ~ %s)", pref.getQuietStart(), pref.getQuietEnd())
                                : "발송 가능")
                        .build();
            }
        }
        
        // 2. 사용자 설정 없으면 → 시스템 정책 적용
        boolean isSystemBlock = systemPolicyService.isBlockTime(checkTime);
        
        log.info("🏢 System policy applied. userId={}, isBlock={}", userId, isSystemBlock);
        
        return QuietTimeCheckResult.builder()
                .isQuietTime(isSystemBlock)
                .reason(isSystemBlock ? "SYSTEM_POLICY" : "ALLOWED")
                .source("SYSTEM_POLICY")
                .userId(userId)
                .channel(channel)
                .checkTime(checkTime)
                .quietStart(null)  // 시스템 정책은 별도 조회 필요
                .quietEnd(null)
                .message(isSystemBlock ? "시스템 금지 시간대 (22:00 ~ 08:00)" : "발송 가능")
                .build();
    }
    
    /**
     * 간단히 금지 시간 여부만 확인
     */
    public boolean isQuietTime(Long userId, String channel) {
        return checkQuietTime(userId, channel).isQuietTime();
    }
    
    // ========================================
    // 사용자 설정 CRUD
    // ========================================
    
    /**
     * 사용자의 모든 알림 설정 조회
     */
    @Cacheable(value = "userPref", key = "#userId", unless = "#result.isEmpty()")
    public List<UserPrefResponse> getUserPrefs(Long userId) {
        List<UserNotificationPref> prefs = prefRepository.findAllByUserId(userId);
        return prefs.stream()
                .map(UserPrefResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 채널 설정 조회
     */
    public Optional<UserNotificationPref> getUserPref(Long userId, String channel) {
        return prefRepository.findByUserIdAndChannel(userId, channel);
    }
    
    /**
     * 알림 설정 생성/수정
     */
    @Transactional
    @CacheEvict(value = "userPref", key = "#request.userId")
    public UserPrefResponse saveOrUpdatePref(UserPrefRequest request) {
        log.info("💾 Saving user pref. userId={}, channel={}", request.getUserId(), request.getChannel());
        
        Optional<UserNotificationPref> existing = prefRepository.findByUserIdAndChannel(
                request.getUserId(), request.getChannel());
        
        UserNotificationPref pref;
        
        if (existing.isPresent()) {
            // 기존 설정 업데이트
            UserNotificationPref old = existing.get();
            pref = UserNotificationPref.builder()
                    .prefId(old.getPrefId())
                    .userId(old.getUserId())
                    .channel(old.getChannel())
                    .enabled(request.getEnabled() != null ? request.getEnabled() : old.getEnabled())
                    .priority(request.getPriority() != null ? request.getPriority() : old.getPriority())
                    .quietStart(request.getQuietStart())
                    .quietEnd(request.getQuietEnd())
                    .createdAt(old.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
        } else {
            // 신규 생성
            pref = UserNotificationPref.builder()
                    .userId(request.getUserId())
                    .channel(request.getChannel())
                    .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                    .priority(request.getPriority() != null ? request.getPriority() : 1)
                    .quietStart(request.getQuietStart())
                    .quietEnd(request.getQuietEnd())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        
        UserNotificationPref saved = prefRepository.save(pref);
        log.info("✅ User pref saved. prefId={}", saved.getPrefId());
        
        return UserPrefResponse.from(saved);
    }
    
    /**
     * 금지 시간대만 업데이트
     */
    @Transactional
    @CacheEvict(value = "userPref", key = "#userId")
    public void updateQuietTime(Long userId, String channel, LocalTime quietStart, LocalTime quietEnd) {
        log.info("⏰ Updating quiet time. userId={}, channel={}, {}~{}", 
                userId, channel, quietStart, quietEnd);
        
        // 설정이 없으면 기본값으로 생성
        if (!prefRepository.existsByUserIdAndChannel(userId, channel)) {
            UserPrefRequest request = UserPrefRequest.builder()
                    .userId(userId)
                    .channel(channel)
                    .enabled(true)
                    .priority(1)
                    .quietStart(quietStart)
                    .quietEnd(quietEnd)
                    .build();
            saveOrUpdatePref(request);
            return;
        }
        
        prefRepository.updateQuietTime(userId, channel, quietStart, quietEnd);
        log.info("✅ Quiet time updated.");
    }
    
    /**
     * 채널 활성화/비활성화
     */
    @Transactional
    @CacheEvict(value = "userPref", key = "#userId")
    public void toggleChannel(Long userId, String channel, boolean enabled) {
        log.info("🔄 Toggling channel. userId={}, channel={}, enabled={}", userId, channel, enabled);
        
        if (!prefRepository.existsByUserIdAndChannel(userId, channel)) {
            UserPrefRequest request = UserPrefRequest.builder()
                    .userId(userId)
                    .channel(channel)
                    .enabled(enabled)
                    .priority(1)
                    .build();
            saveOrUpdatePref(request);
            return;
        }
        
        prefRepository.updateEnabled(userId, channel, enabled);
        log.info("✅ Channel toggled.");
    }
    
    /**
     * 금지 시간대 제거
     */
    @Transactional
    @CacheEvict(value = "userPref", key = "#userId")
    public void removeQuietTime(Long userId, String channel) {
        log.info("🗑️ Removing quiet time. userId={}, channel={}", userId, channel);
        prefRepository.updateQuietTime(userId, channel, null, null);
    }
    
    /**
     * 사용자 알림 설정 전체 삭제
     */
    @Transactional
    @CacheEvict(value = "userPref", key = "#userId")
    public void deleteUserPrefs(Long userId) {
        log.info("🗑️ Deleting all prefs for user. userId={}", userId);
        prefRepository.deleteAllByUserId(userId);
    }
    
    // ========================================
    // 통계/관리용
    // ========================================
    
    /**
     * 금지 시간대 설정된 사용자 목록
     */
    public List<UserPrefResponse> getUsersWithQuietTime() {
        return prefRepository.findAllWithQuietTime().stream()
                .map(UserPrefResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 채널별 활성화된 사용자 수
     */
    public long countEnabledUsers(String channel) {
        return prefRepository.countEnabledByChannel(channel);
    }
}