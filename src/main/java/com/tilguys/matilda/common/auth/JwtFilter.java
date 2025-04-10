package com.tilguys.matilda.common.auth;

import com.tilguys.matilda.common.auth.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private static final String EXPIRED_JWT = "만료된 토큰입니다";

    private final Jwt jwt;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        String token = jwt.getTokenFromCookie(cookies);
        setAuthentication(response, token);

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(HttpServletResponse response, String token) {
        try {
            if (StringUtils.hasText(token) && jwt.validateToken(token)) {
                Authentication authentication = jwt.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String username = authentication.getName();
                validateUsername(username);
            }
        } catch (ExpiredJwtException e) {
            log.info(EXPIRED_JWT + jwt);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void validateUsername(String username) {
        userService.validateExistUser(username);
    }
}
