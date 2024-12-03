package com.example.mate.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker  // WebSocket 메시지 브로커 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 구독 경로 설정 - 클라이언트가 구독할 수 있는 endpoint 설정
        // 클라이언트는 이 prefix로 시작하는 주제를 구독할 수 있음
        registry.enableSimpleBroker(
                "/sub/chat/mate",     // 메이트 채팅 (다대다 채팅)
                "/sub/chat/goods",    // 굿즈거래 채팅 (1:1)
                "/sub/chat/dm"        // 일반 DM (1:1)
        );

        // 발행 경로 설정 - 클라이언트가 메시지를 발행할 때 사용할 prefix
        // 클라이언트가 메시지를 보낼 때는 이 prefix로 시작하는 endpoint로 메시지를 전송
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 endpoint 설정
        // 클라이언트는 이 경로로 WebSocket 연결을 맺음
        registry.addEndpoint("/ws/chat")
                // CORS 설정 - 허용할 origin 패턴 설정
                .setAllowedOriginPatterns("*")
                // SockJS 지원 추가 (WebSocket을 지원하지 않는 브라우저를 위한 fallback)
                .withSockJS();
    }
}