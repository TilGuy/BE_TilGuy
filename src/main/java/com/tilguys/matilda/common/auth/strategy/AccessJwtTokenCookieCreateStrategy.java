package com.tilguys.matilda.common.auth.strategy;

import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.SimpleUserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessJwtTokenCookieCreateStrategy implements JwtCookieCreateStrategy {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = (long) 1000 * 60 * 60; // 1시간
    private final Key key;

    @Override
    public Cookie createCookie(Authentication authentication) {
        String jwtToken = createJwtToken(authentication);
        return createJwtCookie(jwtToken);
    }

    private String createJwtToken(Authentication authentication) {
        SimpleUserInfo simpleUserInfo = (SimpleUserInfo) authentication.getPrincipal();
        Long id = simpleUserInfo.id();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date tokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim(Jwt.getClaimsUserId(), id)
                .claim(Jwt.getAuthoritiesKey(), authorities)
                .claim(Jwt.getNicknameKey(), simpleUserInfo)
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
