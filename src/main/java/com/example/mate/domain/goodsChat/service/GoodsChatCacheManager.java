package com.example.mate.domain.goodsChat.service;

import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
        String cacheKey = formatCacheKey(chatRoomId);
        Double score = convertToScore(message.getSentAt());
        redisTemplate.opsForZSet().add(cacheKey, message, score);
        setTTL(cacheKey);
    }

    // Redis Sorted Set 에서 lastSentAt 이전의 메시지 size 만큼 조회
    public List<GoodsChatMessage> fetchMessagesFromCache(Long chatRoomId, LocalDateTime lastSentAt, int size) {
        String cacheKey = formatCacheKey(chatRoomId);
        Set<GoodsChatMessage> messages;

        // lastSentAt이 null 인 경우, 가장 최근 메시지 조회
        if (lastSentAt == null) {
             messages = redisTemplate.opsForZSet().reverseRange(cacheKey, 0, size - 1);
        } else {
            Double score = convertToScore(lastSentAt);
            messages = redisTemplate.opsForZSet().rangeByScore(cacheKey, Double.NEGATIVE_INFINITY, score, 1, 20);
        }
        return new ArrayList<>(messages);
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

    private Double convertToScore(LocalDateTime sentAt) {
        return (double) sentAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
