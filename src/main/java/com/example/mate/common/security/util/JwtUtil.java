package com.example.mate.common.security.util;

import com.example.mate.common.jwt.JwtToken;
import com.example.mate.domain.member.entity.Member;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret_key}")
    private String key;

    // JWT 서명을 위한 비밀 키 생성
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    // JWT 토큰 생성
    public JwtToken createTokens(Member member) {
        Map<String, Object> payloadMap = member.getPayload();
        Date now = new Date();
        payloadMap.put("iat", System.currentTimeMillis());

        String accessToken = createAccessToken(payloadMap, now);
        String refreshToken = createRefreshToken(member.getId(), now);
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // JWT Access Token 생성. 2분 유효시간
    public String createAccessToken(Map<String, Object> valueMap, Date issuedAt) {
        SecretKey key = getSigningKey();

        return Jwts.builder()
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + Duration.ofMinutes(2).toMillis()))
                .setClaims(valueMap)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT Access Token 생성. 3일 유효시간
    public String createRefreshToken(Long memberId, Date issuedAt) {
        SecretKey key = getSigningKey();

        return Jwts.builder()
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + Duration.ofMinutes(60 * 24 * 3).toMillis())) // 3일 유효
                .setClaims(Map.of("memberId", memberId))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰의 서명을 검증하고 클레임 반환
    public Map<String, Object> validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
