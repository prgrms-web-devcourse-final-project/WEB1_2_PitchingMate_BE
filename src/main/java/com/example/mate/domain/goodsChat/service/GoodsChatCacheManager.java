package com.example.mate.domain.goodsChat.service;

import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class GoodsChatCacheManager {

    private final RedisTemplate<String, GoodsChatMessage> redisTemplate;

    private static final String CACHE_KEY_FORMAT = "goods_chat_message::%d";
    private static final long DEFAULT_TTL_SECONDS = 3600;

    public GoodsChatCacheManager(
            @Qualifier("goodsChatCacheRedisTemplate") RedisTemplate<String, GoodsChatMessage> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Redis Sorted Set 에 메시지를 저장
    public void storeMessageInCache(Long chatRoomId, GoodsChatMessage message) {
        storeMessagesInCache(chatRoomId, List.of(message));
    }

    // Redis Sorted Set 에 메시지 List 저장
    public void storeMessagesInCache(Long chatRoomId, List<GoodsChatMessage> messages) {
        for (GoodsChatMessage message : messages) {
            String cacheKey = formatCacheKey(chatRoomId);
            Double score = convertToScore(message.getSentAt());
            redisTemplate.opsForZSet().add(cacheKey, message, score);
        }
        setTTL(formatCacheKey(chatRoomId));
    }

    /**
     * Redis 에서 특정 채팅방의 메시지를 lastSentAt 기준으로 최신순으로 정렬하여 조회합니다.
     * @param chatRoomId 채팅방 ID (해당 채팅방의 메시지를 조회합니다.)
     * @param lastSentAt 메시지를 조회할 기준 시간 (null 일 경우 가장 최근 메시지를 조회합니다.)
     * @param size 조회할 메시지의 개수
     * @return 최신순으로 정렬된 조회된 메시지 List (메시지가 없으면 빈 리스트를 반환합니다.)
     */
    public List<GoodsChatMessage> fetchMessagesFromCache(Long chatRoomId, LocalDateTime lastSentAt, int size) {
        String cacheKey = formatCacheKey(chatRoomId);
        Set<GoodsChatMessage> messages;

        // lastSentAt이 null 인 경우, 가장 최근 메시지 조회
        if (lastSentAt == null) {
             messages = redisTemplate.opsForZSet().reverseRange(cacheKey, 0, size - 1);
        // lastSentAt을 기준으로 이전의 메시지를 최신순으로 정렬하여 조회
        } else {
            Double score = convertToScore(lastSentAt);
            messages = redisTemplate.opsForZSet().reverseRangeByScore(cacheKey, Double.NEGATIVE_INFINITY, score, 1, size);
        }

        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(messages);
    }

    // Redis Sorted Set 에서 모든 메시지를 삭제
    public void evictMessagesFromCache(Long chatRoomId) {
        String cacheKey = formatCacheKey(chatRoomId);
        redisTemplate.opsForZSet().removeRange(cacheKey, 0, -1);
    }

    // TTL 설정 - 1시간
    private void setTTL(String cacheKey) {
        redisTemplate.expire(cacheKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private String formatCacheKey(Long chatRoomId) {
        return String.format(CACHE_KEY_FORMAT, chatRoomId);
    }

    // LocalDateTime 을 Redis Sorted Set 에서 사용하는 score 값으로 변환
    private Double convertToScore(LocalDateTime sentAt) {
        return (double) sentAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
