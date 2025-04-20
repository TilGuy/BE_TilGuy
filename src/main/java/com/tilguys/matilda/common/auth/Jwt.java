package com.tilguys.matilda.common.auth;


import com.tilguys.matilda.common.auth.exception.InvalidJwtToken;
import com.tilguys.matilda.common.auth.exception.MatildaException;
import com.tilguys.matilda.common.auth.strategy.JwtCookieCreateStrategy;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Jwt {

    private static final String INVALID_AUTH_TOKEN = "유효하지 않은 인증 토큰입니다.";
    private static final String REFRESH_COOKIE_HEADER = "refreshToken";
    private static final String COOKIE_NAME = "jwt";
    private static final String AUTHORITIES_KEY = "Authorization";
    private static final String INVALID_JWT_SIGN = "잘못된 JWT 서명입니다.";
    private static final String NOT_SUPPORTED_JWT = "지원되지 않는 JWT 토큰입니다.";
    private static final String INVALID_JWT_ARGUMENT = "JWT 토큰이 잘못되었습니다.";
    private static final String CLAIMS_USER_ID = "userId";
    private static final long REFRESH_TOKEN_TIME = 60 * 60 * 24 * 7;

    private final JwtCookieCreateStrategy jwtCookieCreateStrategy;
    private final Key key;

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

    public boolean validateToken(String token) throws ExpiredJwtException {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info(INVALID_JWT_SIGN);
        } catch (UnsupportedJwtException e) {
            log.info(NOT_SUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            log.info(INVALID_JWT_ARGUMENT);
        } catch (ExpiredJwtException e) {
            log.info("token expired : " + e.getMessage());
        }
        log.info("token : " + token);
        return false;
    }

    private Claims parseClaims(String accessToken) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
    }

    public String getSubjectFromToken(String accessToken) {
        Claims claims = parseClaims(accessToken);
        return claims.getSubject();
    }

    public String getPrincipleFromToken(String token) {
        return getSubjectFromToken(token);
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

    public String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_COOKIE_HEADER.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new MatildaException(INVALID_AUTH_TOKEN);
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 사용자 ID를 Long으로 변환하여 principal로 사용
        Long userId = Long.parseLong(claims.getSubject());

        // User 객체 대신 Long 타입의 userId를 principal로 사용
        return new UsernamePasswordAuthenticationToken(userId, "", authorities);
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

    public static String getAuthoritiesKey() {
        return AUTHORITIES_KEY;
    }

    public static String getCookieName() {
        return COOKIE_NAME;
    }

    public static String getClaimsUserId() {
        return CLAIMS_USER_ID;
    }

//
//    public String getNewAccessCode(HttpServletRequest request)
//    {
//        String refreshToken = resolveRefreshToken(request);
//        if (StringUtils.hasText(refreshToken) && jwtTokenFactory.validateToken(refreshToken)) {
//            Authentication authentication = jwtTokenFactory.getAuthentication(refreshToken);
//            return jwtTokenFactory.generateAccessToken(authentication);
//        }
//        throw new MatildaException(INVALID_AUTH_TOKEN);
//    }
//
//    public void deleteRefreshCookie(HttpServletResponse response)
//    {
//        Cookie deletedRefreshTokenCookie = new Cookie("refreshToken", null);
//        deletedRefreshTokenCookie.setMaxAge(0);
//        deletedRefreshTokenCookie.setPath("/"); // 쿠키의 경로를 설정합니다. 필요에 따라 경로를 조정하세요.
//        response.addCookie(deletedRefreshTokenCookie);
//    }
//
//    public static String generateRandomPassword() {
//        int minLength = 8;
//        int maxLength = 20;
//
//        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";
//
//        SecureRandom random = new SecureRandom();
//        StringBuilder password = new StringBuilder();
//
//        int passwordLength = minLength + random.nextInt(maxLength - minLength + 1);
//
//        for (int i = 0; i < passwordLength; i++) {
//            int randomIndex = random.nextInt(characters.length());
//            char randomChar = characters.charAt(randomIndex);
//
//            password.append(randomChar);
//        }
//
//        return password.toString();
//    }
//
//    public String getUsernameFromToken(String token) {
//        return jwtTokenFactory.getUsernameFromToken(token);
//    }
}
