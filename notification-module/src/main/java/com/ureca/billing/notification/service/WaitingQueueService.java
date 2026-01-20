package com.ureca.billing.notification.service;

import com.ureca.billing.notification.domain.dto.WaitingQueueStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitingQueueService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String QUEUE_KEY = "queue:message:waiting";
    
    /**
     * 대기열에 메시지 추가 (JSON 문자열)
     */
    public void addToQueue(String messageJson) {
        try {
            LocalDateTime releaseTime = calculateReleaseTime();
            long score = releaseTime.atZone(ZoneId.systemDefault()).toEpochSecond();
            
            redisTemplate.opsForZSet().add(QUEUE_KEY, messageJson, score);
            
            log.info("📥 대기열 저장 완료. releaseTime={}", releaseTime);
            
        } catch (Exception e) {
            log.error("❌ 대기열 저장 실패: {}", e.getMessage());
            throw new RuntimeException("Failed to add to queue", e);
        }
    }
    
    /**
     * 발송 가능한 메시지 조회 (현재 시간 이전)
     */
    public Set<String> getReadyMessages(int limit) {
        long now = System.currentTimeMillis() / 1000;
        
        Set<String> messages = redisTemplate.opsForZSet()
                .rangeByScore(QUEUE_KEY, 0, now, 0, limit);
        
        log.info("📤 대기열 조회 - {}건", messages != null ? messages.size() : 0);
        
        return messages;
    }
    
    /**
     * 대기열에서 메시지 제거
     */
    public void removeFromQueue(String messageJson) {
        Long removed = redisTemplate.opsForZSet().remove(QUEUE_KEY, messageJson);
        log.debug("🗑️ 대기열 제거 - {}건", removed);
    }
    
    /**
     * 대기열 크기 확인
     */
    public long getQueueSize() {
        Long size = redisTemplate.opsForZSet().size(QUEUE_KEY);
        return size != null ? size : 0;
    }

    /**
     * 대기열 전체 삭제 (테스트용)
     */
    public void clearQueue() {
        Boolean deleted = redisTemplate.delete(QUEUE_KEY);
        log.info("🗑️ 대기열 초기화. deleted={}", deleted);
    }
    
    /**
     * 대기열 상태 조회
     */
    public WaitingQueueStatus getQueueStatus() {
        Long totalCount = redisTemplate.opsForZSet().size(QUEUE_KEY);
        
        long now = System.currentTimeMillis() / 1000;
        Long readyCount = redisTemplate.opsForZSet().count(QUEUE_KEY, 0, now);
        
        Set<String> readyMessages = getReadyMessages(10);
        
        List<String> messageList = readyMessages != null 
                ? readyMessages.stream().limit(10).collect(Collectors.toList())
                : List.of();
        
        return WaitingQueueStatus.builder()
                .totalCount(totalCount != null ? totalCount : 0)
                .queueKey(QUEUE_KEY)
                .readyCount(readyCount != null ? readyCount : 0)
                .readyMessages(messageList)
                .build();
    }
    
    /**
     * 다음 발송 가능 시간 계산 (다음날 08:00)
     */
    private LocalDateTime calculateReleaseTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.toLocalDate().plusDays(1).atTime(8, 0);
    }
}