package com.tilguys.matilda.user.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 프로필_이미지_테스트() throws Exception {
        TilUser user = TilUser.builder()
                .avatarUrl("https://avatars.githubusercontent.com/u/101252011?v=4")
                .identifier("praisebak")
                .nickname("praisebak")
                .providerInfo(ProviderInfo.GITHUB)
                .role(Role.USER).build();

        TilUser savedUser = userRepository.save(user);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profileUrl/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"avatarUrl\":\"https://avatars.githubusercontent.com/u/101252011?v=4\"}"));
    }
}
