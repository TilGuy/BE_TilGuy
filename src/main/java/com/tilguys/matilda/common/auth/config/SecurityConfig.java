package com.tilguys.matilda.common.auth.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.service.AuthService;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.common.auth.strategy.AccessJwtTokenCookieCreateStrategy;
import com.tilguys.matilda.common.auth.strategy.JwtCookieCreateStrategy;
import com.tilguys.matilda.til.service.UserRefreshTokenService;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private static final String[] PERMITTED_ROLES = {"USER"};

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${jwt.secret}")
    private String secret;

    private final UserService userService;
    private final UserRefreshTokenService userRefreshTokenService;
    private final AuthService authService;

    @Bean
    public Key jwtKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Bean
    public JwtCookieCreateStrategy jwtCookieCreateStrategy() {
        return new AccessJwtTokenCookieCreateStrategy(jwtKey());
    }

    @Bean
    public Jwt jwt() {
        return new Jwt(jwtCookieCreateStrategy(), jwtKey());
    }

    @Bean
    public PrevLoginFilter prevLoginFilter() {
        return new PrevLoginFilter(jwt(), userService, userRefreshTokenService, authService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .addFilterBefore(prevLoginFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/oauth/login")
                        .permitAll()
                        .anyRequest()
                        .hasAnyRole(PERMITTED_ROLES));
        return http.build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(frontendUrl)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("Content-Type")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
