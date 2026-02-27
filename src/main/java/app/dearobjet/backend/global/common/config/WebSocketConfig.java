package app.dearobjet.backend.global.common.config;

import app.dearobjet.backend.domain.chat.websocket.StompChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP 설정
 * 스케일 아웃 환경에서는 Redis Pub/Sub을 통해 메시지 브로드캐스트
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompChannelInterceptor stompChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 접속 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 구독(수신) prefix (서버 -> 클라이언트)
        // /topic: 1:N 브로드캐스트 (채팅방 메시지)
        // /queue: 1:1 개인 메시지 (알림 등)
        registry.enableSimpleBroker("/topic", "/queue");

        // 메시지 발행(송신) prefix (클라이언트 -> 서버)
        registry.setApplicationDestinationPrefixes("/app");

        // 사용자별 개인 메시지 prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // JWT 인증 인터셉터 등록
        registration.interceptors(stompChannelInterceptor);
    }
}
