package com.tilguys.matilda.common.auth;


import com.tilguys.matilda.common.auth.exception.InvalidJwtToken;
import com.tilguys.matilda.common.auth.service.AuthService;
import com.tilguys.matilda.common.auth.strategy.JwtCookieCreateStrategy;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import java.security.Key;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Jwt {

    private static final String COOKIE_NAME = "jwt";
    private static final String AUTHORITIES_KEY = "Authorization";
    private static final String CLAIMS_USER_ID = "userId";
    private static final String NICKNAME_KEY = "nickname";
    private static final long REFRESH_TOKEN_TIME = (long) 60 * 60 * 24 * 7;

    private final JwtCookieCreateStrategy jwtCookieCreateStrategy;
    private final Key key;
    private final AuthService authService;

    public Cookie createJwtCookie() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return jwtCookieCreateStrategy.createCookie(authentication);
    }

    public String getTokenFromCookie(Cookie[] cookies) {
        if (cookies != null) {
            return findValidToken(cookies);
        }
        return "";
    }

    private static String findValidToken(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private Claims parseClaims(String accessToken) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        // Try to get from dedicated userId claim first
        if (claims.get(CLAIMS_USER_ID) != null) {
            return Long.parseLong(claims.get(CLAIMS_USER_ID).toString());
        }
        // Fallback to subject if userId claim is not present
        return Long.parseLong(claims.getSubject());
    }

    public Long resolveUserIdWhenJwtExpired(ExpiredJwtException e) {
        Object idObject = e.getClaims().get(Jwt.getClaimsUserId());

        // Number로 캐스팅 후 longValue() 메서드 사용
        if (idObject instanceof Number) {
            return ((Number) idObject).longValue();
        }
        throw new InvalidJwtToken("jwt id는 숫자여야합니다.");
    }

    public static long getRefreshTokenAliveSecond() {
        return REFRESH_TOKEN_TIME;
    }

    public static String getNicknameKey() {
        return NICKNAME_KEY;
    }

    public static String getAuthoritiesKey() {
        return AUTHORITIES_KEY;
    }

    public static String getCookieName() {
        return COOKIE_NAME;
    }

    public static String getClaimsUserId() {
        return CLAIMS_USER_ID;
    }
}
