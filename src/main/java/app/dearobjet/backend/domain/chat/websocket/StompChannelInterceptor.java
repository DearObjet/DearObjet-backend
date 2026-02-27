package app.dearobjet.backend.domain.chat.websocket;

import app.dearobjet.backend.global.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * STOMP 메시지 인터셉터
 * WebSocket 연결 시 JWT 토큰 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // CONNECT 시 JWT 토큰 검증
            String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                log.warn("WebSocket connection attempt without valid Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());

            if (!jwtProvider.validateToken(token)) {
                log.warn("WebSocket connection attempt with invalid JWT token");
                throw new IllegalArgumentException("Invalid JWT token");
            }

            Long userId = jwtProvider.getUserId(token);
            String role = jwtProvider.getRole(token).name();

            // Principal 설정 (인증 정보)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            accessor.setUser(authentication);
            log.info("WebSocket CONNECT - userId: {}, role: {}", userId, role);
        }

        return message;
    }
}
