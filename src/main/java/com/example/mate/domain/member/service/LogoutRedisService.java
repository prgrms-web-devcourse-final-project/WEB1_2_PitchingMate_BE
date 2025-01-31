package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LogoutRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    public LogoutRedisService(
            @Qualifier("jwtTokenRedisTemplate") RedisTemplate<String, String> redisTemplate,
            JwtUtil jwtUtil
    ) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    // 로그아웃 시 액세스 토큰의 남은 시간만큼 블랙리스트에 추가
    public void addTokenToBlacklist(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
        }

        String accessToken = authorizationHeader.substring(7);
        Map<String, Object> claims = jwtUtil.validateToken(accessToken);

        long remainingTime = (long) claims.get("iat") + Duration.ofMinutes(30).toMillis() - new Date().getTime();

        redisTemplate.opsForValue().set(
                "blacklist:" + accessToken, "blacklisted", remainingTime, TimeUnit.MILLISECONDS);
    }

    // 블랙리스트에 해당 액세스 토큰 있는지 확인
    public boolean isTokenBlacklisted(String accessToken) {
        return redisTemplate.hasKey("blacklist:" + accessToken);
    }
}
