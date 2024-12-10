package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.security.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutRedisServiceTest {

    @InjectMocks
    private LogoutRedisService logoutRedisService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("회원 로그아웃")
    class LogoutMember {

        @Test
        @DisplayName("회원 로그아웃 성공 - 블랙리스트에 토큰 추가")
        void add_token_to_blacklist_success() {
            // given
            String token = "Bearer accessToken";
            String accessToken = token.substring(7);

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            long iat = System.currentTimeMillis();  // 현재 시간으로 iat 값 설정
            Map<String, Object> claims = Map.of("iat", iat);
            given(jwtUtil.validateToken(accessToken)).willReturn(claims);

            lenient().doNothing().when(valueOperations).set(
                    "blacklist:" + accessToken,
                    "blacklisted",
                    120000L,
                    TimeUnit.MILLISECONDS
            );

            // when & then
            assertDoesNotThrow(() -> logoutRedisService.addTokenToBlacklist(token));
        }

        @Test
        @DisplayName("회원 로그아웃 실패 - 잘못된 토큰으로 블랙리스트 추가 시 CustomException")
        void add_token_to_blacklist_fail_invalid_token() {
            // given
            String invalidToken = "InvalidToken";

            // when & then
            assertThatThrownBy(() -> logoutRedisService.addTokenToBlacklist(invalidToken))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.INVALID_AUTH_TOKEN.getMessage());
        }
    }

    @Nested
    @DisplayName("블랙리스트에 토큰 여부 확인")
    class CheckBlacklist {

        @Test
        @DisplayName("블랙리스트에 토큰 여부 확인 성공")
        void is_token_blacklisted_success() {
            // given
            String accessToken = "accessToken";
            when(redisTemplate.hasKey("blacklist:" + accessToken)).thenReturn(true);

            // when
            boolean result = logoutRedisService.isTokenBlacklisted(accessToken);

            // then
            verify(redisTemplate, times(1)).hasKey("blacklist:accessToken");
            assert result;
        }

        @Test
        @DisplayName("블랙리스트에 토큰 여부 확인 실패 - 블랙리스트에 존재하지 않는 토큰 확인")
        void is_token_blacklisted_fail_not_exists_token() {
            // given
            String accessToken = "accessToken";
            when(redisTemplate.hasKey("blacklist:" + accessToken)).thenReturn(false);

            // when
            boolean result = logoutRedisService.isTokenBlacklisted(accessToken);

            // then
            verify(redisTemplate, times(1)).hasKey("blacklist:accessToken");
            assert !result;
        }
    }
}
