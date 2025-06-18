package com.tilguys.matilda.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.tilguys.matilda.auth.strategy.TestJwtTokenCookieCreateStrategy;
import com.tilguys.matilda.auth.user.WithMockCustomUser;
import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.SimpleUserInfo;
import com.tilguys.matilda.common.auth.UserRefreshToken;
import com.tilguys.matilda.common.auth.repository.UserRefreshTokenRepository;
import com.tilguys.matilda.common.auth.service.AuthService;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import java.security.Key;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Jwt jwt;

    @Autowired
    private UserRefreshTokenRepository userRefreshTokenRepository;

    @Autowired
    private Key jwtKey;

    @Autowired
    private AuthService authService;

    private Jwt expireCreateJwt;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        this.expireCreateJwt = new Jwt(new TestJwtTokenCookieCreateStrategy(jwtKey), jwtKey, authService);
    }

    @Test
    void 유효한_JWT_토큰이_없으면_로그인_제외_권한_부족이다() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/til"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockCustomUser(identifier = 1L)
    void 만료된_JWT_토큰일시_403_발생할_수_있다() throws Exception {
        Cookie jwtCookie = expireCreateJwt.createJwtCookie();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/til")
                        .cookie(jwtCookie))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockCustomUser(identifier = 1L)
    void 만료된_JWT_토큰일시_로그인에서_403_에러는_뜨지_않는다() throws Exception {
        Cookie jwtCookie = expireCreateJwt.createJwtCookie();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/oauth/login")
                        .cookie(jwtCookie))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).isNotEqualTo(403);
                });
    }

    @Test
    @WithMockCustomUser(identifier = 1L)
    @Transactional
    void 유효한_JWT_토큰이_있으면_유저_권한으로_요청가능하다() throws Exception {
        TilUser user = TilUser.builder()
                .avatarUrl("https://avatars.githubusercontent.com/u/101252011?v=4")
                .identifier("praisebak")
                .nickname("praisebak")
                .providerInfo(ProviderInfo.GITHUB)
                .role(Role.USER).build();

        userRepository.save(user);

        Cookie jwtCookie = jwt.createJwtCookie();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/oauth/logout")
                        .cookie(jwtCookie))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Transactional
    void 만료된_ACCESS_TOKEN일시_REFRESH_TOKEN를_확인하여_유효하면_ACCESS_TOKEN을_갱신한다() throws Exception {
        TilUser user = TilUser.builder()
                .avatarUrl("https://avatars.githubusercontent.com/u/101252011?v=4")
                .identifier("praisebak")
                .nickname("praisebak")
                .providerInfo(ProviderInfo.GITHUB)
                .role(Role.USER).build();
        TilUser savedUser = userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new SimpleUserInfo(
                savedUser.getId(), savedUser.getNickname()), authService.createAuthentication(savedUser)));

        Cookie jwtCookie = expireCreateJwt.createJwtCookie();
        UserRefreshToken userRefreshToken = UserRefreshToken.builder()
                .userId(savedUser.getId())
                .expireDate(LocalDateTime.now().plusHours(1L))
                .build();

        userRefreshTokenRepository.save(userRefreshToken);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/oauth/logout").cookie(jwtCookie))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).isNotEqualTo(403);
                });
    }
}
