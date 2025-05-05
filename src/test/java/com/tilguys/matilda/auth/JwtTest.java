package com.tilguys.matilda.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.tilguys.matilda.auth.user.WithMockCustomUser;
import com.tilguys.matilda.common.auth.Jwt;
import jakarta.servlet.http.Cookie;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@ActiveProfiles("test")
class JwtTest {

    @Autowired
    private Jwt jwt;

    @Test
    @WithMockCustomUser(identifier = 1L)
    void 로그인시_깃허브_아이디를_담은_JWT를_반환한다() {
        Cookie jwtCookie = jwt.createJwtCookie();
        Assertions.assertThat(jwtCookie.getName()).isNotNull();
    }

    @Test
    @WithMockCustomUser(identifier = 1L)
    void JWT로_유저를_식별할_수_있다() {
        Cookie jwtCookie = jwt.createJwtCookie();
        String token = jwtCookie.getValue();
        Long userIdFromToken = jwt.getUserIdFromToken(token);
        assertThat(userIdFromToken).isEqualTo(1L);
    }
}
