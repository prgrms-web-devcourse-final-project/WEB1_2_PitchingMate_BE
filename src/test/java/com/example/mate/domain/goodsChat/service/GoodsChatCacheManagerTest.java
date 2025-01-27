package com.example.mate.domain.goodsChat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
public class GoodsChatCacheManagerTest {

    @InjectMocks private GoodsChatCacheManager goodsChatCacheManager;
    @Mock private RedisTemplate<String, GoodsChatMessage> redisTemplate;
    @Mock private ZSetOperations<String, GoodsChatMessage> zSetOperations;

    private GoodsChatMessage message1;
    private GoodsChatMessage message2;

    @BeforeEach
    public void setUp() {
        message1 = createGoodsChatMessage(LocalDateTime.now());
        message2 = createGoodsChatMessage(LocalDateTime.now().minusMinutes(10));
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    private GoodsChatMessage createGoodsChatMessage(LocalDateTime sentAt) {
        return GoodsChatMessage.builder()
                .content("test message")
                .chatRoomId(1L)
                .memberId(1L)
                .messageType(MessageType.TALK)
                .sentAt(sentAt)
                .build();
    }

    @Test
    @DisplayName("단건 채팅 메시지를 Redis 캐시에 저장한다.")
    public void store_message_in_cache_success() {
        // given
        Long chatRoomId = 1L;
        String cacheKey = String.format("goods_chat_message::%d", chatRoomId);

        // when
        goodsChatCacheManager.storeMessageInCache(chatRoomId, message1);

        // then
        verify(zSetOperations).add(eq(cacheKey), eq(message1), any(Double.class));
        verify(redisTemplate).expire(eq(cacheKey), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("다수의 채팅 메시지를 Redis 캐시에 저장한다.")
    public void store_messages_in_cache_success() {
        // given
        Long chatRoomId = 1L;
        String cacheKey = String.format("goods_chat_message::%d", chatRoomId);

        // when
        goodsChatCacheManager.storeMessagesInCache(chatRoomId, Arrays.asList(message1, message2));

        // then
        verify(zSetOperations).add(eq(cacheKey), eq(message1), any(Double.class));
        verify(zSetOperations).add(eq(cacheKey), eq(message2), any(Double.class));
        verify(redisTemplate).expire(eq(cacheKey), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Redis 캐시에서 마지막 메시지를 기준으로 오래된 메시지를 조회한다.")
    public void fetch_messages_from_cache_with_last_sent_at_success() {
        // given
        Long chatRoomId = 1L;
        String cacheKey = String.format("goods_chat_message::%d", chatRoomId);
        LocalDateTime lastSentAt = LocalDateTime.now();
        Double score = (double) lastSentAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Set<GoodsChatMessage> mockMessages = new LinkedHashSet<>(Arrays.asList(message2, message1));
        when(zSetOperations.rangeByScore(eq(cacheKey), eq(Double.NEGATIVE_INFINITY), eq(score), eq(1L), eq(20L)))
                .thenReturn(mockMessages);

        // when
        List<GoodsChatMessage> result = goodsChatCacheManager.fetchMessagesFromCache(chatRoomId, lastSentAt, 20);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(message2, message1);
        verify(zSetOperations).rangeByScore(eq(cacheKey), eq(Double.NEGATIVE_INFINITY), eq(score), eq(1L), eq(20L));
    }

    @Test
    @DisplayName("마지막 메시지가 없는 경우, Redis 캐시에서 최신 메시지를 조회한다.")
    public void fetch_messages_from_cache_success() {
        // given
        Long chatRoomId = 1L;
        String cacheKey = String.format("goods_chat_message::%d", chatRoomId);

        Set<GoodsChatMessage> mockMessages = new LinkedHashSet<>(Arrays.asList(message2, message1));
        when(zSetOperations.reverseRange(eq(cacheKey), eq(0L), eq(19L))).thenReturn(mockMessages);

        // when
        List<GoodsChatMessage> result = goodsChatCacheManager.fetchMessagesFromCache(chatRoomId, null, 20);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(message2, message1);
        verify(zSetOperations).reverseRange(eq(cacheKey), eq(0L), eq(19L));
    }

    @Test
    @DisplayName("Redis 캐시에서 모든 채팅 메시지를 삭제한다.")
    public void evict_messages_from_cache_success() {
        // given
        Long chatRoomId = 1L;
        String cacheKey = String.format("goods_chat_message::%d", chatRoomId);

        // when
        goodsChatCacheManager.evictMessagesFromCache(chatRoomId);

        // then
        verify(zSetOperations).removeRange(cacheKey, 0, -1);
    }
}