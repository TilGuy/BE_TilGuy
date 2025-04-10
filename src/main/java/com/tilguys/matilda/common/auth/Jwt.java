package com.tilguys.matilda.common.auth;


import com.tilguys.matilda.common.auth.exception.MatildaException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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

    private final Key key;
    private final JwtTokenFactory jwtTokenFactory;

    public Jwt(String secretKey, JwtTokenFactory jwtTokenFactory) {
        this.jwtTokenFactory = jwtTokenFactory;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Cookie createJwtCookie() {
        return jwtTokenFactory.createJwtCookieWithKey(key);
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
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info(INVALID_JWT_SIGN);
        } catch (UnsupportedJwtException e) {
            log.info(NOT_SUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            log.info(INVALID_JWT_ARGUMENT);
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

    public static String getCookieName() {
        return COOKIE_NAME;
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

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
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

