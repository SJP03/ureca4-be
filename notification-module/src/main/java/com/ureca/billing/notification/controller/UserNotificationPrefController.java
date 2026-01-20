package com.ureca.billing.notification.controller;

import com.ureca.billing.notification.domain.dto.QuietTimeCheckResult;
import com.ureca.billing.notification.domain.dto.UserPrefRequest;
import com.ureca.billing.notification.domain.dto.UserPrefResponse;
import com.ureca.billing.notification.service.UserQuietTimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Tag(name = "User Notification Preferences", description = "사용자별 알림 설정 및 금지 시간대 관리 API")
@RestController
@RequestMapping("/api/user-prefs")
@RequiredArgsConstructor
@Slf4j
public class UserNotificationPrefController {
    
    private final UserQuietTimeService quietTimeService;
    
    // ========================================
    // 금지 시간 체크 API
    // ========================================
    
    @Operation(summary = "금지 시간 체크", description = "현재 시간이 사용자의 금지 시간대인지 확인")
    @GetMapping("/{userId}/check-quiet")
    public ResponseEntity<QuietTimeCheckResult> checkQuietTime(
            @PathVariable Long userId,
            @Parameter(description = "채널 (EMAIL, SMS, PUSH)") @RequestParam(defaultValue = "EMAIL") String channel) {
        
        QuietTimeCheckResult result = quietTimeService.checkQuietTime(userId, channel);
        return ResponseEntity.ok(result);
    }
    
    @Operation(summary = "특정 시간으로 금지 시간 체크 (테스트용)")
    @GetMapping("/{userId}/check-quiet-at")
    public ResponseEntity<QuietTimeCheckResult> checkQuietTimeAt(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "EMAIL") String channel,
            @Parameter(description = "테스트 시간 (HH:mm 형식)") @RequestParam String time) {
        
        LocalTime checkTime = LocalTime.parse(time);
        QuietTimeCheckResult result = quietTimeService.checkQuietTime(userId, channel, checkTime);
        return ResponseEntity.ok(result);
    }
    

    // ========================================
    // 설정 저장/수정 API
    // ========================================
    
    @Operation(summary = "알림 설정 저장/수정", description = "사용자의 채널별 알림 설정을 생성하거나 수정")
    @PostMapping
    public ResponseEntity<UserPrefResponse> saveOrUpdatePref(@RequestBody UserPrefRequest request) {
        log.info("📝 Save/Update pref request: {}", request);
        
        UserPrefResponse response = quietTimeService.saveOrUpdatePref(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "금지 시간대 설정", description = "특정 사용자의 채널에 금지 시간대만 설정")
    @PutMapping("/{userId}/{channel}/quiet-time")
    public ResponseEntity<Map<String, Object>> setQuietTime(
            @PathVariable Long userId,
            @PathVariable String channel,
            @Parameter(description = "금지 시작 시간 (HH:mm)") @RequestParam String quietStart,
            @Parameter(description = "금지 종료 시간 (HH:mm)") @RequestParam String quietEnd) {
        
        LocalTime start = LocalTime.parse(quietStart);
        LocalTime end = LocalTime.parse(quietEnd);
        
        quietTimeService.updateQuietTime(userId, channel, start, end);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("금지 시간대가 설정되었습니다: %s ~ %s", quietStart, quietEnd),
                "userId", userId,
                "channel", channel,
                "quietStart", quietStart,
                "quietEnd", quietEnd
        ));
    }
    
    @Operation(summary = "금지 시간대 제거")
    @DeleteMapping("/{userId}/{channel}/quiet-time")
    public ResponseEntity<Map<String, Object>> removeQuietTime(
            @PathVariable Long userId,
            @PathVariable String channel) {
        
        quietTimeService.removeQuietTime(userId, channel);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "금지 시간대가 제거되었습니다.",
                "userId", userId,
                "channel", channel
        ));
    }
    
    @Operation(summary = "채널 활성화/비활성화")
    @PutMapping("/{userId}/{channel}/toggle")
    public ResponseEntity<Map<String, Object>> toggleChannel(
            @PathVariable Long userId,
            @PathVariable String channel,
            @RequestParam boolean enabled) {
        
        quietTimeService.toggleChannel(userId, channel, enabled);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", enabled ? "채널이 활성화되었습니다." : "채널이 비활성화되었습니다.",
                "userId", userId,
                "channel", channel,
                "enabled", enabled
        ));
    }
    

}