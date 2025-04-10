package com.tilguys.matilda.common.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenFactory {

    private static final String AUTHORITIES_KEY = "Authorization";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30 min

    private String createJwt(Authentication authentication, String authorities, Date tokenExpiresIn, Key key) {
        User user = (User) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(tokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String generateAccessToken(Authentication authentication, Key key) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date tokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return createJwt(authentication, authorities, tokenExpiresIn, key);
    }

    public Cookie createJwtCookieWithKey(Key key) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String jwtToken = generateAccessToken(authentication, key);
        return createJwtCookie(jwtToken);
    }

    private Cookie createJwtCookie(String jwtToken) {
        Cookie cookie = new Cookie(Jwt.getCookieName(), jwtToken);
        cookie.setHttpOnly(true);  // JavaScript로 접근 불가
        cookie.setSecure(false);    // HTTPS에서만 전송
        cookie.setPath("/");       // 해당 경로에만 유효
        cookie.setMaxAge((int) ACCESS_TOKEN_EXPIRE_TIME);    // 만료 시간 (초 단위)
        return cookie;
    }
}

