package app.dearobjet.backend.global.auth.security;

import app.dearobjet.backend.global.auth.entrypoint.RestAuthenticationEntryPoint;
import app.dearobjet.backend.global.auth.jwt.JwtAuthenticationFilter;
import app.dearobjet.backend.global.auth.oauth.CustomOAuth2UserService;
import app.dearobjet.backend.global.auth.oauth.OAuthSuccessHandler;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@Profile("prod")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuthSuccessHandler oAuthSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/token/refresh",
                                "/auth/login",
                                "/oauth/**",
                                "/oauth2/authorization/**",
                                "/login/oauth2/**",
                                "/error",
                                "/ws/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/notices/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/shops/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/shops/map/**").permitAll()

                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuthSuccessHandler)
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 주소 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 쿠키/인증 정보 허용 (매우 중요!)
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

