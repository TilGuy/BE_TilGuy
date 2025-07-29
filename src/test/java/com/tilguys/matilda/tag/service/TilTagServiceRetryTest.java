package com.tilguys.matilda.tag.service;

import com.tilguys.matilda.common.external.FailoverAIServiceManager;
import com.tilguys.matilda.tag.repository.SubTagRepository;
import com.tilguys.matilda.tag.repository.TagRepository;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.event.TilCreatedEvent;
import com.tilguys.matilda.til.service.TilService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TilTagServiceRetryTest {

    @Mock
    private FailoverAIServiceManager failoverAIServiceManager;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private SubTagRepository subTagRepository;

    @Mock
    private TilService tilService;

    @Mock
    private TagCreationOutboxService tagCreationOutboxService;

    @Mock
    private Til mockTil;

    private TilTagService tilTagService;
    private TilCreatedEvent testEvent;

    @BeforeEach
    void setUp() {
        tilTagService = new TilTagService(
                failoverAIServiceManager,
                tagRepository,
                subTagRepository,
                tilService,
                tagCreationOutboxService
        );

        testEvent = new TilCreatedEvent(1L, "Test TIL content", 100L);

        // Mock TIL 설정
        given(tilService.getTilByTilId(1L)).willReturn(mockTil);
    }

    @Test
    @DisplayName("첫 번째 시도에서 성공 - 1번만 호출")
    void createTagsWithRetry_FirstAttemptSuccess_CallOnce() {
        // given
        String successResponse = """
                {
                    "choices": [{
                        "message": {
                            "tool_calls": [{
                                "function": {
                                    "arguments": "{\\"tags\\": [\\"Java\\", \\"Spring\\"]}"
                                }
                            }]
                        }
                    }]
                }
                """;

        given(failoverAIServiceManager.callAIWithSimpleFallback(any(), any()))
                .willReturn(successResponse);

        Tag mockTag = new Tag("Java");
        given(tagRepository.saveAll(any())).willReturn(List.of(mockTag));
        given(subTagRepository.saveAll(any())).willReturn(List.of());

        // when
        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> tilTagService.createTagsWithRetry(testEvent));
        long endTime = System.currentTimeMillis();

        // then
        verify(failoverAIServiceManager, times(1)).callAIWithSimpleFallback(any(), any());
        verify(tagRepository, times(1)).saveAll(any());

        // 첫 번째 시도에서 성공했으므로 1초 대기 없이 빠르게 완료되어야 함
        assertTrue(endTime - startTime < 500, "첫 번째 시도 성공 시 빠르게 완료되어야 함");
    }

    @Test
    @DisplayName("첫 번째 실패, 두 번째 성공 - 정확히 2번 호출 및 1초 대기")
    void createTagsWithRetry_FirstFailSecondSuccess_CallTwiceWithDelay() {
        // given
        String successResponse = """
                {
                    "choices": [{
                        "message": {
                            "tool_calls": [{
                                "function": {
                                    "arguments": "{\\"tags\\": [\\"Java\\", \\"Spring\\"]}"
                                }
                            }]
                        }
                    }]
                }
                """;

        // 첫 번째 호출은 실패, 두 번째 호출은 성공
        given(failoverAIServiceManager.callAIWithSimpleFallback(any(), any()))
                .willThrow(new RuntimeException("첫 번째 시도 실패"))
                .willReturn(successResponse);

        Tag mockTag = new Tag("Java");
        given(tagRepository.saveAll(any())).willReturn(List.of(mockTag));
        given(subTagRepository.saveAll(any())).willReturn(List.of());

        // when
        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> tilTagService.createTagsWithRetry(testEvent));
        long endTime = System.currentTimeMillis();

        // then
        verify(failoverAIServiceManager, times(2)).callAIWithSimpleFallback(any(), any());
        verify(tagRepository, times(1)).saveAll(any());

        // 1초 대기 + 처리 시간으로 1초 이상 걸려야 함
        assertTrue(endTime - startTime >= 1000, "재시도 시 1초 대기 시간이 있어야 함");
        assertTrue(endTime - startTime < 2000, "불필요한 지연이 없어야 함");
    }

    @Test
    @DisplayName("두 번 모두 실패 - 정확히 2번 호출 후 예외 발생")
    void createTagsWithRetry_BothAttemptsFail_CallTwiceAndThrowException() {
        // given
        given(failoverAIServiceManager.callAIWithSimpleFallback(any(), any()))
                .willThrow(new RuntimeException("첫 번째 시도 실패"))
                .willThrow(new RuntimeException("두 번째 시도 실패"));

        // when & then
        long startTime = System.currentTimeMillis();
        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> {
                    tilTagService.createTagsWithRetry(testEvent);
                }
        );
        long endTime = System.currentTimeMillis();

        // 예외 메시지 확인
        assertTrue(exception.getMessage()
                .contains("태그 생성 실패 (모든 재시도 소진)"));
        assertTrue(exception.getMessage()
                .contains("두 번째 시도 실패"));

        // 정확히 2번 호출되었는지 확인
        verify(failoverAIServiceManager, times(2)).callAIWithSimpleFallback(any(), any());
        verify(tagRepository, never()).saveAll(any());

        // 1초 대기 시간이 포함되어야 함
        assertTrue(endTime - startTime >= 1000, "재시도 시 1초 대기 시간이 있어야 함");
    }

    @Test
    @DisplayName("maxAttempts = 2 설정 검증 - 정확히 2번만 시도")
    void createTagsWithRetry_MaxAttemptsTwo_ExactlyTwoAttempts() {
        // given
        given(failoverAIServiceManager.callAIWithSimpleFallback(any(), any()))
                .willThrow(new RuntimeException("항상 실패"));

        // when
        assertThrows(
                RuntimeException.class, () -> {
                    tilTagService.createTagsWithRetry(testEvent);
                }
        );

        // then
        // 정확히 2번만 호출되어야 함 (3번째 시도는 없어야 함)
        verify(failoverAIServiceManager, times(2)).callAIWithSimpleFallback(any(), any());

        // 다른 메서드들은 호출되지 않아야 함
        verify(tagRepository, never()).saveAll(any());
        verify(subTagRepository, never()).saveAll(any());
        verify(mockTil, never()).updateTags(any());
    }
}
