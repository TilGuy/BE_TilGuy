package com.tilguys.matilda.common.auth.config;

import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.service.AuthService;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.til.service.UserRefreshTokenService;
import com.tilguys.matilda.user.TilUser;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class PrevLoginFilter extends OncePerRequestFilter {

    private final Jwt jwt;
    private final UserService userService;
    private final UserRefreshTokenService userRefreshTokenService;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        clearSecurityContext();

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwt.getTokenFromCookie(cookies);
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            resolveJwtToken(token);
        } catch (ExpiredJwtException e) {
            clearJwtToken(response);
            Long id = jwt.resolveUserIdWhenJwtExpired(e);
            userRefreshTokenReLogin(id, response);
        }
        filterChain.doFilter(request, response);
    }

    private void clearSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private static void clearJwtToken(HttpServletResponse response) {
        Cookie jwt = new Cookie(Jwt.getCookieName(), null);
        jwt.setMaxAge(0); // 쿠키의 expiration 타임을 0으로 하여 없앤다.
        jwt.setPath("/"); // 모든 경로에서 삭제 됬음을 알린다.
        response.addCookie(jwt);
    }

    private void setSecurityContext(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void resolveJwtToken(String token) {
        Long userId = jwt.getUserIdFromToken(token);
        Optional<TilUser> userByIdentifier = userService.findById(userId);
        if (userByIdentifier.isEmpty()) {
            clearSecurityContext();
            return;
        }

        Authentication authentication = createAuthentication(userByIdentifier.get());
        setSecurityContext(authentication);
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

    private Authentication createAuthentication(TilUser tilUser) {
        List<SimpleGrantedAuthority> authorities = authService.createAuthorities(List.of(tilUser.getRole()));
        return new UsernamePasswordAuthenticationToken(tilUser.getId(), "", authorities);
    }
}
