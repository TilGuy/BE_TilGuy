package com.tilguys.matilda.auth.strategy;

import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.SimpleUserInfo;
import com.tilguys.matilda.common.auth.strategy.JwtCookieCreateStrategy;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import java.security.Key;
import java.util.Date;
import org.springframework.security.core.Authentication;


public class TestJwtTokenCookieCreateStrategy implements JwtCookieCreateStrategy {

    private final Key key;

    public TestJwtTokenCookieCreateStrategy(Key key) {
        this.key = key;
    }

    @Override
    public Cookie createCookie(Authentication authentication) {
        SimpleUserInfo simpleUserInfo = (SimpleUserInfo) authentication.getPrincipal();

        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 1000L); // 1초 전 = 이미 만료됨

        String token = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(Jwt.getClaimsUserId(), simpleUserInfo.id())
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return new Cookie("jwt", token);
    }
}
