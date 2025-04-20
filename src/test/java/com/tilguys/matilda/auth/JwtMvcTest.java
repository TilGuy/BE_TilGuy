package com.tilguys.matilda.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.tilguys.matilda.auth.strategy.TestJwtTokenCookieCreateStrategy;
import com.tilguys.matilda.auth.user.WithMockCustomUser;
import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.repository.UserRefreshTokenRepository;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import java.security.Key;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JwtMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Jwt jwt;

    @Autowired
    private UserRefreshTokenRepository userRefreshTokenRepository;

    @Autowired
    private Key jwtKey;

    private Jwt expireCreateJwt;

    @BeforeEach
    void setup() {
        this.expireCreateJwt = new Jwt(new TestJwtTokenCookieCreateStrategy(jwtKey), jwtKey);
    }

    @BeforeAll
    static void init(@Autowired UserRepository userRepositoryParam) {
        final TilUser TIL_USER = TilUser.builder()
                .identifier("praisebak")
                .providerInfo(ProviderInfo.GITHUB)
                .role(Role.USER)
                .build();
        userRepositoryParam.save(TIL_USER);
    }

    @Test
    @WithMockCustomUser(identifier = 1L)
    void 로그인시_깃허브_아이디를_담은_jwt를_반환한다() {
        Cookie jwtCookie = jwt.createJwtCookie();
        assertThat(jwtCookie.getName()).isNotNull();
    }

    @Test
    @WithMockCustomUser(identifier = 1L)
    void JWT로_유저를_식별할_수_있다() {
        Cookie jwtCookie = jwt.createJwtCookie();
        String token = jwtCookie.getValue();
        Authentication authentication = jwt.getAuthentication(token);

        Long principal = (Long) authentication.getPrincipal();

        assertThat(principal).isEqualTo(1L);
    }

    @Test
    void 유효한_JWT_토큰이_없으면_로그인_제외_권한_부족이다() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/oauth/logout"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockCustomUser(identifier = 1L)
    void 만료된_JWT_토큰일시_403_발생할_수_있다() throws Exception {
        Cookie jwtCookie = expireCreateJwt.createJwtCookie();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/oauth/logout")
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
    void 유효한_JWT_토큰이_있으면_유저_권한으로_요청가능하다() throws Exception {
        Cookie jwtCookie = jwt.createJwtCookie();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/oauth/logout")
                        .cookie(jwtCookie))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

//TODO
//    @Test
//    @WithMockCustomUser(identifier = 1L)
//    void 만료된_ACCESS_TOKEN일시_REFRESH_TOKEN를_확인하여_유효하면_ACCESS_TOKEN을_갱신한다() throws Exception {
//        Cookie jwtCookie = expireCreateJwt.createJwtCookie();
//        UserRefreshToken userRefreshToken = UserRefreshToken.builder()
//                .userId(1L)
//                .expireDate(LocalDateTime.now().plusHours(1L))
//                .build();
//        userRefreshTokenRepository.save(userRefreshToken);
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/oauth/logout")
//                        .cookie(jwtCookie))
//                .andExpect(result -> {
//                    int status = result.getResponse().getStatus();
//                    assertThat(status).isNotEqualTo(403);
//                });
//    }
}
