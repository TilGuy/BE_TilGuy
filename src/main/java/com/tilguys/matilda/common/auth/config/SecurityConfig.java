package com.tilguys.matilda.common.auth.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.service.AuthService;
import com.tilguys.matilda.common.auth.service.UserRefreshTokenService;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.common.auth.strategy.AccessJwtTokenCookieCreateStrategy;
import com.tilguys.matilda.common.auth.strategy.JwtCookieCreateStrategy;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    private final UserService userService;
    private final UserRefreshTokenService userRefreshTokenService;
    private final AuthService authService;
    @Value("${frontend.url}")
    private String frontendUrl;
    @Value("${jwt.secret}")
    private String secret;

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
        return new Jwt(jwtCookieCreateStrategy(), jwtKey(), authService);
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
                        .requestMatchers(HttpMethod.GET, "/api/til/range").hasAnyAuthority(PERMITTED_ROLES)
                        .requestMatchers(HttpMethod.GET, "/api/til/**").permitAll()
                        .requestMatchers(
                                "/api/oauth/login",
                                "/api/oauth/logout",
                                "/api/user/profileUrl/**",
                                "/actuator/**",
                                "/error"
                        ).permitAll()
                        .anyRequest()
                        .hasAnyAuthority(PERMITTED_ROLES));
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
