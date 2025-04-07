package com.tilguys.matilda;

import com.tilguys.matilda.auth.WithMockCustomUser;
import com.tilguys.matilda.config.jwt.Jwt;
import com.tilguys.matilda.config.jwt.JwtFilter;
import com.tilguys.matilda.service.UserService;
import jakarta.servlet.http.Cookie;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@AutoConfigureMockMvc
class JwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Jwt jwt;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtFilter jwtFilter;

    @Test
    @WithMockCustomUser(identifier = "praisebak")
    void 로그인시_깃허브_아이디를_담은_jwt를_반환한다() {
        Cookie jwtCookie = jwt.createJwtCookie();
        Assertions.assertThat(jwtCookie.getName()).isNotNull();
    }

    @Test
    @WithMockCustomUser(identifier = "praisebak")
    void jwt로_유저를_식별할_수_있다() {
        Cookie jwtCookie = jwt.createJwtCookie();
        String token = jwtCookie.getValue();
        Authentication authentication = jwt.getAuthentication(token);

        Assertions.assertThat(authentication.getPrincipal()).isEqualTo("praisebak");
    }


    @Test
    void 유효한_JWT_토큰이_없으면_로그인_제외_권한_부족() {
        Assertions.assertThatThrownBy(
                        () -> mockMvc.perform(MockMvcRequestBuilders.get("/api/oauth/logout")
                                        .cookie(jwtCookie))
                                .andExpect(MockMvcResultMatchers.status().isUnauthorized()))
                .doesNotThrowAnyException();
    }

    @Test
    void 유효한_JWT_토큰이_있으면_유저_권한으로_요청가능() {
        Cookie jwtCookie = jwt.createJwtCookie();
        Assertions.assertThatThrownBy(
                        () -> mockMvc.perform(MockMvcRequestBuilders.get("/api/oauth/logout")
                                        .cookie(jwtCookie))
                                .andExpect(MockMvcResultMatchers.status().isOk()))
                .doesNotThrowAnyException();
    }

}
