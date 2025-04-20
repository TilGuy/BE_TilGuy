package com.tilguys.matilda.common.auth.strategy;

import com.tilguys.matilda.common.auth.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessJwtTokenCookieCreateStrategy implements JwtCookieCreateStrategy {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1시간
    private final Key key;

    @Override
    public Cookie createCookie(Authentication authentication) {
        String jwtToken = createJwtToken(authentication);
        return createJwtCookie(jwtToken);
    }

    private String createJwtToken(Authentication authentication) {
        Long id = (Long) authentication.getPrincipal();

        long now = (new Date()).getTime();
        Date tokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim(Jwt.getClaimsUserId(), id)
                .setExpiration(tokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
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
