package app.dearobjet.backend.global.auth.entrypoint;

import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.api.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<ErrorResponse> body =
                ApiResponse.of(new ErrorResponse("로그인이 필요합니다"));

        response.getWriter().write(
                objectMapper.writeValueAsString(body)
        );
    }
}
