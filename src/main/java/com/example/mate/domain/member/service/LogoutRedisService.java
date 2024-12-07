package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LogoutRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // 로그아웃 시 블랙리스트에 토큰 추가
    public void addTokenToBlacklist(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
        }

        // TODO : 테스트용 1분 유효를 변경
        redisTemplate.opsForValue().set("blacklist:" + token.substring(7), "blacklisted", 1, TimeUnit.MINUTES);
    }

    // 블랙리스트에 토큰 있는지 확인
    public boolean isTokenBlacklisted(String accessToken) {
        return redisTemplate.hasKey("blacklist:" + accessToken);
    }
}
