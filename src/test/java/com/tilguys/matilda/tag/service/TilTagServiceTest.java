package com.tilguys.matilda.tag.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.tilguys.matilda.common.external.OpenAIClient;
import com.tilguys.matilda.tag.domain.SubTag;
import com.tilguys.matilda.tag.domain.TilTags;
import com.tilguys.matilda.tag.repository.SubTagRepository;
import com.tilguys.matilda.tag.repository.TagRepository;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.repository.TilRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@ActiveProfiles("test")
class TilTagServiceTest {

    private TilTagService tilTagService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TilRepository tilRepository;

    @Autowired
    private SubTagRepository subTagRepository;
    private String tagResponseJson = """
            {
              "id" : "chatcmpl-BkQcDzauFTV7XQhxrEPIrYn0UuOdI",
              "object" : "chat.completion",
              "created" : 1750405097,
              "model" : "gpt-4o-2024-05-13",
              "choices" : [ {
                "index" : 0,
                "message" : {
                  "role" : "assistant",
                  "content" : null,
                  "tool_calls" : [ {
                    "id" : "call_vxWh6ZUmuOyRPYRQ7WNBFwbd",
                    "type" : "function",
                    "function" : {
                      "name" : "extractTilTags",
                      "arguments" : "{\\"tags\\":[\\"테스트\\",\\"객체지향\\",\\"설계\\",\\"리팩터링\\",\\"문서화\\"],\\"subTags\\":[{\\"coreTag\\":\\"테스트\\",\\"subTag\\":\\"Mockito\\"},{\\"coreTag\\":\\"테스트\\",\\"subTag\\":\\"독립적인 테스트\\"},{\\"coreTag\\":\\"테스트\\",\\"subTag\\":\\"when/retrun\\"},{\\"coreTag\\":\\"테스트\\",\\"subTag\\":\\"verify() 메서드\\"},{\\"coreTag\\":\\"객체지향\\",\\"subTag\\":\\"디미터 법칙\\"},{\\"coreTag\\":\\"객체지향\\",\\"subTag\\":\\"SRP 원칙\\"},{\\"coreTag\\":\\"설계\\",\\"subTag\\":\\"계층 분리\\"},{\\"coreTag\\":\\"리팩터링\\",\\"subTag\\":\\"유연성\\"},{\\"coreTag\\":\\"리팩터링\\",\\"subTag\\":\\"확장성\\"},{\\"coreTag\\":\\"문서화\\",\\"subTag\\":\\"테스트 문서화\\"}]}"
                    }
                  } ],
                  "refusal" : null,
                  "annotations" : [ ]
                },
                "logprobs" : null,
                "finish_reason" : "stop"
              } ],
              "usage" : {
                "prompt_tokens" : 863,
                "completion_tokens" : 176,
                "total_tokens" : 1039,
                "prompt_tokens_details" : {
                  "cached_tokens" : 0,
                  "audio_tokens" : 0
                },
                "completion_tokens_details" : {
                  "reasoning_tokens" : 0,
                  "audio_tokens" : 0,
                  "accepted_prediction_tokens" : 0,
                  "rejected_prediction_tokens" : 0
                }
              },
              "service_tier" : "default",
              "system_fingerprint" : "fp_9fd01826bf"
            }""";

    public TilTagServiceTest(@Autowired TagRepository tagRepository,
                             @Autowired SubTagRepository subTagRepository) {
        OpenAIClient openAIClient = Mockito.mock(OpenAIClient.class);
        when(openAIClient.callOpenAI(any(), any())).thenReturn(tagResponseJson);
        this.tilTagService = new TilTagService(openAIClient, tagRepository, subTagRepository);
    }

    @BeforeEach
    void cleanUp() {
        subTagRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    void 지정한_날짜로_태그들을_가져올_수_있다() {
        Til til = Til.builder()
                .content("asdf")
                .date(LocalDate.now())
                .isDeleted(false)
                .isPublic(false)
                .tags(new ArrayList<>())
                .tilUser(null)
                .title("asdf")
                .build();
        Tag tag = new Tag(null, "Asdf", til);

        til.updateTags(List.of(tag));
        tilRepository.save(til);

        assertThat(tagRepository.findAll().size()).isEqualTo(1L);
        LocalDate startDay = LocalDate.now().minusDays(7L);

        assertThat(tilTagService.getRecentWroteTags(startDay).size()).isEqualTo(1L);
    }

    @Test
    void responseJson에_맞춰_핵심_태그를_추출할_수_있다() {
        List<Tag> tags = tilTagService.saveTilTags(tagResponseJson);
        assertThat(tags.size()).isEqualTo(5L);
    }

    @Test
    void til_tags와_함께_서브_태그들을_추출할_수_있다() {
        List<Tag> tags = tilTagService.saveTilTags(tagResponseJson);
        TilTags tilTags = new TilTags(tags);

        List<SubTag> subTags = tilTagService.createSubTags(tagResponseJson, tilTags);

        assertThat(subTags.size()).isEqualTo(10L);
    }

    @Test
    void 날짜_이후로_생성된_서브태그를_가져올_수_있다() {
        List<Tag> tags = tilTagService.saveTilTags(tagResponseJson);
        TilTags tilTags = new TilTags(tags);

        Til til = new Til(null, null, "asdf", "asdf", null, false, false, null, null);
        tilRepository.save(til);

        for (Tag tag : tags) {
            tag.setTil(til);
            tagRepository.save(tag);
        }

        List<SubTag> subTags = tilTagService.createSubTags(tagResponseJson, tilTags);

        List<SubTag> recentSubTags = tilTagService.getRecentSubTags(LocalDate.from(LocalDateTime.now().minusDays(2L)));

        assertThat(recentSubTags.size()).isEqualTo(subTags.size());
    }

    @Test
    void 태그및_서브_태그들을_response_string_기반으로_파싱_및_저장할_수_있다() {
        List<Tag> tags = tilTagService.saveTilTags(tagResponseJson);
        TilTags tilTags = new TilTags(tags);

        List<SubTag> subTags = tilTagService.createSubTags(tagResponseJson, tilTags);

        assertThat(tagRepository.count()).isEqualTo(tags.size());
        assertThat(subTagRepository.count()).isEqualTo(subTags.size());
    }

    @Test
    void til_content로_태그_생성_요청하여_그에_맞는_json을_반환받을_수_있다() {
        assertThat(tilTagService.requestTilTagResponseJson("tmpTilContent")).isNotEmpty();
    }
}
