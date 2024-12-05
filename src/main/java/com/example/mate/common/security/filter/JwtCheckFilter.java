package com.example.mate.common.security.filter;

import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.common.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtCheckFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // 필터링 적용하지 않을 URI 체크
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        // TODO : 2024/11/28 - 각 도메인별로 인증이 필요없는 경로 추가

        // 사용자 로그인, 회원가입 경로 인증 제외 (토큰 발급 경로)
        if (isAuthExcludedPath(request)) {
            return true;
        }

        // 메인 페이지 인증 제외
        if (isMainPagePath(request)) {
            return true;
        }

        return false;
    }

    // 필터링 적용 - 액세스 토큰 확인
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String headerAuth = request.getHeader("Authorization");

        // 액세스 토큰 유효성 검사
        if (!isTokenValid(headerAuth)) {
            handleException(response, new Exception("ACCESS TOKEN NOT FOUND"));
            return;
        }

        // 토큰 유효성 검증 후 SecurityContext 설정
        String accessToken = headerAuth.substring(7); // "Bearer " 제외한 토큰 저장
        try {
            Map<String, Object> claims = jwtUtil.validateToken(accessToken);
            setAuthentication(claims);  // 인증 정보 설정
            filterChain.doFilter(request, response); // 검증 결과 문제가 없는 경우 요청 처리
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    // 사용자 로그인 또는 회원가입 경로인지 확인
    private boolean isAuthExcludedPath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 소셜 로그인/회원 가입 경로, mate 서비스 로그인/회원 가입 경로 인증 제외
        return requestURI.startsWith("/api/auth") ||
                requestURI.startsWith("/api/members/join") ||
                requestURI.startsWith("/swagger-ui") ||
                requestURI.startsWith("/api/members/login");
    }

    // 메인 페이지의 인증 필요없는 메서드인지 확인
    private boolean isMainPagePath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // 메인 페이지와 관련된 경로
        return requestURI.startsWith("/api/matches/main") ||
                requestURI.startsWith("/api/mates/main") ||
                requestURI.startsWith("/api/goods/main") ||
                requestURI.startsWith("/api/teams/rankings") ||
                requestURI.startsWith("/api/matches/team/") && requestURI.endsWith("/completed");
    }

    // 액세스 토큰 유효성 검사
    private boolean isTokenValid(String headerAuth) {
        return headerAuth != null && headerAuth.startsWith("Bearer ");
    }

    // SecurityContext에 인증 정보 설정
    private void setAuthentication(Map<String, Object> claims) {
        String userId = claims.get("email").toString();
        String[] roles = claims.get("role").toString().split(","); // role이 여러개일 수 있으므로
        Long memberId = Long.valueOf(claims.get("memberId").toString());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                new AuthMember(userId, memberId),
                null, // 이미 인증되었으므로 null
                Arrays.stream(roles)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList())
        );

        SecurityContextHolder.getContext().setAuthentication(authToken); // SecurityContext에 인증 정보 저장
    }

    // 403 Forbidden 에러 처리
    public void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().println("{\"error\": \"" + e.getMessage() + "\"}");
    }
}