package com.tilguys.matilda.auth;

import com.tilguys.matilda.auth.user.WithMockCustomUser;
import com.tilguys.matilda.common.auth.Jwt;
import jakarta.servlet.http.Cookie;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@ActiveProfiles("test")
public class JwtTest {

    @Autowired
    private Jwt jwt;

    @Test
    @WithMockCustomUser(identifier = "praisebak")
    void 로그인시_깃허브_아이디를_담은_JWT를_반환한다() {
        Cookie jwtCookie = jwt.createJwtCookie();
        Assertions.assertThat(jwtCookie.getName()).isNotNull();
    }

    @Test
    @WithMockCustomUser(identifier = "praisebak")
    void JWT로_유저를_식별할_수_있다() {
        Cookie jwtCookie = jwt.createJwtCookie();
        String token = jwtCookie.getValue();
        Authentication authentication = jwt.getAuthentication(token);

        User principal = (User) authentication.getPrincipal();
        Assertions.assertThat(principal.getUsername()).isEqualTo("1");
    }
}
