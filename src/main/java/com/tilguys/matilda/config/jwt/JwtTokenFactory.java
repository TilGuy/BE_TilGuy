package com.tilguys.matilda.config.jwt;

import com.tilguys.matilda.exception.InvalidJwtToken;
import com.tilguys.matilda.exception.MatildaException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenFactory {

    private static final String AUTHORITIES_KEY = "Authorization";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30 min
    private static final int ONE_DAY = 3600 * 24;
    private static final int ONE_MIN = 60;

    private static final String INVALID_TOKEN = "유효하지 않은 토큰입니다.";

    private static final String INVALID_JWT_SIGN = "잘못된 JWT 서명입니다.";
    private static final String NOT_SUPPORTED_JWT = "지원되지 않는 JWT 토큰입니다.";
    private static final String INVALID_JWT_ARGUMENT = "JWT 토큰이 잘못되었습니다.";
    private final Key key;

    public JwtTokenFactory(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String resolveUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    private String createJwt(Authentication authentication, String authorities, Date tokenExpiresIn) {
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(tokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new MatildaException(INVALID_TOKEN);
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
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

    public String generateAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date tokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return createJwt(authentication, authorities, tokenExpiresIn);
    }

    public Cookie createJwtCookie(Authentication authentication) {
        String jwtToken = generateAccessToken(authentication);
        return createJwtCookie(jwtToken);
    }

    private Cookie createJwtCookie(String jwtToken) {
        Cookie cookie = new Cookie(Jwt.getCookieName(), jwtToken);
        cookie.setHttpOnly(true);  // JavaScript로 접근 불가
        cookie.setSecure(false);    // HTTPS에서만 전송
        cookie.setPath("/");       // 해당 경로에만 유효
        cookie.setMaxAge(ONE_DAY);    // 만료 시간 (초 단위)
        return cookie;
    }

    public String resolveJwtToken(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if (name.equals(Jwt.getCookieName())) {
                return cookie.getAttribute(Jwt.getCookieName());
            }
        }
        throw new InvalidJwtToken(INVALID_TOKEN);
    }
}

