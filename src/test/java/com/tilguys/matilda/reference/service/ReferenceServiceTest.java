package com.tilguys.matilda.reference.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilguys.matilda.common.external.OpenAIClient;
import com.tilguys.matilda.common.external.FailoverAIServiceManager;
import com.tilguys.matilda.reference.event.ReferenceCreateEvent;
import com.tilguys.matilda.reference.repository.ReferenceRepository;
import com.tilguys.matilda.til.domain.Reference;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.domain.TilFixture;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.TilUserFixture;
import com.tilguys.matilda.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReferenceServiceTest {

    private ReferenceService service;

    @Autowired
    private ReferenceRepository referenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TilRepository tilRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public ReferenceServiceTest(@Autowired ReferenceRepository referenceRepository,
                                @Autowired TilRepository tilRepository,
                                @Autowired ObjectMapper objectMapper) {
        String mockOpenAIResponse = createRealisticOpenAIResponse();

        FailoverAIServiceManager mockFailoverManager = Mockito.mock(FailoverAIServiceManager.class);
        when(mockFailoverManager.callAIWithSimpleFallback(any(), any())).thenReturn(mockOpenAIResponse);

        this.service = new ReferenceService(
                mockFailoverManager,
                referenceRepository,
                objectMapper,
                tilRepository
        );
    }

    @Test
    void 레퍼런스_생성() {
        TilUser tilUser = TilUserFixture.createTilUserFixture();
        userRepository.save(tilUser);
        Til til = TilFixture.createTilFixture(tilUser, true, false);
        tilRepository.save(til);

        ReferenceCreateEvent event = new ReferenceCreateEvent(til.getTilId(), "test");
        service.createReference(event);

        List<Reference> results = referenceRepository.getAllByTil_TilId(til.getTilId());
        assertThat(results).isNotEmpty();

    }


    private String createRealisticOpenAIResponse() {
        return """
                {
                  "id": "chatcmpl-example123",
                  "object": "chat.completion",
                  "created": 1750405097,
                  "model": "gpt-4o-2024-05-13",
                  "choices": [{
                    "index": 0,
                    "message": {
                      "role": "assistant",
                      "content": null,
                      "tool_calls": [{
                        "id": "call_reference_extract",
                        "type": "function",
                        "function": {
                          "name": "extractReferences",
                          "arguments": "{\\"references\\":[{\\"word\\":\\"Spring Boot\\",\\"info\\":\\"Spring Boot는 Java 기반의 웹 애플리케이션을 빠르게 개발할 수 있게 해주는 프레임워크입니다.\\"},{\\"word\\":\\"JPA\\",\\"info\\":\\"Java Persistence API로, 자바에서 관계형 데이터베이스를 객체지향적으로 다룰 수 있게 해주는 스펙입니다.\\"},{\\"word\\":\\"Mockito\\",\\"info\\":\\"Java에서 단위 테스트를 위한 모킹 프레임워크로, 가짜 객체를 생성하여 테스트를 수행할 수 있게 해줍니다.\\"}]}"
                        }
                      }],
                      "refusal": null
                    },
                    "finish_reason": "stop"
                  }],
                  "usage": {
                    "prompt_tokens": 100,
                    "completion_tokens": 50,
                    "total_tokens": 150
                  }
                }
                """;
    }
}
