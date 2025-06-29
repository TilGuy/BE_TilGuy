package com.tilguys.matilda.til.controller;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tilguys.matilda.common.auth.config.PrevLoginFilter;
import com.tilguys.matilda.til.service.TilService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class TilControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TilService tilService;

    @MockitoBean
    private PrevLoginFilter prevLoginFilter;

    @Test
    void size가_100을_초과하면_validation_에러가_발생한다() throws Exception {
        // given
        int invalidSize = 101;

        // when && then
        mockMvc.perform(get("/api/til/all")
                        .param("size", String.valueOf(invalidSize)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(HandlerMethodValidationException.class,
                        result.getResolvedException()));
    }

    @Test
    void size가_100_이하면_정상_응답한다() throws Exception {
        // given
        int validSize = 100;

        when(tilService.getPublicTils(any(), any(), eq(validSize)))
                .thenReturn(Collections.emptyList());

        // when && then
        mockMvc.perform(get("/api/til/all")
                        .param("size", String.valueOf(validSize)))
                .andExpect(status().isOk());
    }
}
