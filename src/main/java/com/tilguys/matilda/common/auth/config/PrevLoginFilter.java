package com.tilguys.matilda.common.auth.config;

import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.service.AuthService;
import com.tilguys.matilda.common.auth.service.UserRefreshTokenService;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.user.TilUser;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class PrevLoginFilter extends OncePerRequestFilter {

    private final Jwt jwt;
    private final UserService userService;
    private final UserRefreshTokenService userRefreshTokenService;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            clearSecurityContext();

            String token = extractToken(request, response, filterChain);
            if (token == null) {
                return;
            }
            resolveJwtToken(response, token);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            filterChain.doFilter(request, response);
            return null;
        }

        String token = jwt.getTokenFromCookie(cookies);
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return null;
        }
        return token;
    }

    private void resolveJwtToken(HttpServletResponse response, String token) {
        try {
            Long userId = jwt.getUserIdFromToken(token);
            Optional<TilUser> userByIdentifier = userService.findById(userId);
            if (userByIdentifier.isEmpty()) {
                clearSecurityContext();
                return;
            }

            Authentication authentication = authService.createAuthentication(userByIdentifier.get());
            setSecurityContext(authentication);
        } catch (ExpiredJwtException e) {
            clearJwtToken(response);
            Long id = jwt.resolveUserIdWhenJwtExpired(e);
            userRefreshTokenReLogin(id, response);
        }
    }

    private void clearSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private void setSecurityContext(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static void clearJwtToken(HttpServletResponse response) {
        Cookie jwt = new Cookie(Jwt.getCookieName(), null);
        jwt.setMaxAge(0); // 쿠키의 expiration 타임을 0으로 하여 없앤다.
        jwt.setPath("/"); // 모든 경로에서 삭제 됬음을 알린다.
        response.addCookie(jwt);
    }

    private void userRefreshTokenReLogin(Long id, HttpServletResponse response) {
        boolean refreshSuccess = userRefreshTokenService.isReLoginPossible(id);
        if (refreshSuccess) {
            Authentication authentication = authService.createAuthenticationFromId(id);
            setSecurityContext(authentication);
            Cookie jwtCookie = jwt.createJwtCookie();
            response.addCookie(jwtCookie);
            return;
        }
        clearSecurityContext();
    }
}
