package com.tilguys.matilda.common.external;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FailoverAIServiceManagerTest {

    @Mock
    private AIClient firstClient;

    @Mock
    private AIClient secondClient;

    private FailoverAIServiceManager failoverManager;

    private List<Map<String, Object>> testMessages;
    private Map<String, Object> testFunctionDefinition;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 준비
        testMessages = List.of(
                Map.of("role", "user", "content", "test message")
        );
        testFunctionDefinition = Map.of(
                "name", "test_function",
                "description", "test function"
        );

        // Mock 클라이언트들의 기본 동작 설정
        given(firstClient.getClientName()).willReturn("FirstClient");
        given(secondClient.getClientName()).willReturn("SecondClient");

        // 두 클라이언트로 FailoverAIServiceManager 생성
        failoverManager = new FailoverAIServiceManager(List.of(firstClient, secondClient));
    }

    @Test
    @DisplayName("첫 번째 클라이언트 성공 시 바로 응답 반환")
    void callAIWithSimpleFallback_FirstClientSuccess_ReturnsResult() {
        // given
        String expectedResult = "First client success";
        given(firstClient.callAI(testMessages, testFunctionDefinition))
                .willReturn(expectedResult);

        // when
        String result = failoverManager.callAIWithSimpleFallback(testMessages, testFunctionDefinition);

        // then
        assertEquals(expectedResult, result);
        verify(firstClient, times(1)).callAI(testMessages, testFunctionDefinition);
        verify(secondClient, times(0)).callAI(any(), any()); // 두 번째 클라이언트는 호출되지 않음
    }

    @Test
    @DisplayName("첫 번째 실패, 두 번째 성공 시 두 번째 결과 반환")
    void callAIWithSimpleFallback_FirstFailSecondSuccess_ReturnsSecondResult() {
        // given
        String expectedResult = "Second client success";
        given(firstClient.callAI(testMessages, testFunctionDefinition))
                .willThrow(new RuntimeException("First client failed"));
        given(secondClient.callAI(testMessages, testFunctionDefinition))
                .willReturn(expectedResult);

        // when
        String result = failoverManager.callAIWithSimpleFallback(testMessages, testFunctionDefinition);

        // then
        assertEquals(expectedResult, result);
        verify(firstClient, times(1)).callAI(testMessages, testFunctionDefinition);
        verify(secondClient, times(1)).callAI(testMessages, testFunctionDefinition);
    }

    @Test
    @DisplayName("모든 클라이언트 실패 시 RuntimeException 발생")
    void callAIWithSimpleFallback_AllClientsFail_ThrowsRuntimeException() {
        // given
        RuntimeException firstException = new RuntimeException("First client failed");
        RuntimeException secondException = new RuntimeException("Second client failed");

        given(firstClient.callAI(testMessages, testFunctionDefinition))
                .willThrow(firstException);
        given(secondClient.callAI(testMessages, testFunctionDefinition))
                .willThrow(secondException);

        // when & then
        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> {
                    failoverManager.callAIWithSimpleFallback(testMessages, testFunctionDefinition);
                }
        );

        // 예외 메시지 확인
        assertEquals("All AI services failed", exception.getMessage());
        assertEquals(secondException, exception.getCause()); // 마지막 예외가 원인으로 설정됨

        // 두 클라이언트 모두 호출되었는지 확인
        verify(firstClient, times(1)).callAI(testMessages, testFunctionDefinition);
        verify(secondClient, times(1)).callAI(testMessages, testFunctionDefinition);

        // getClientName이 로깅을 위해 호출되었는지 확인 (생성자 + 로깅)
        verify(firstClient, atLeast(2)).getClientName(); // 생성자 + "Attempting" + "Failed" 로그
        verify(secondClient, atLeast(2)).getClientName(); // 생성자 + "Attempting" + "Failed" 로그
    }

    @Test
    @DisplayName("단일 클라이언트 실패 시에도 적절한 예외 발생")
    void callAIWithSimpleFallback_SingleClientFails_ThrowsRuntimeException() {
        // given
        FailoverAIServiceManager singleClientManager = new FailoverAIServiceManager(List.of(firstClient));
        RuntimeException clientException = new RuntimeException("Single client failed");

        given(firstClient.callAI(testMessages, testFunctionDefinition))
                .willThrow(clientException);

        // when & then
        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> {
                    singleClientManager.callAIWithSimpleFallback(testMessages, testFunctionDefinition);
                }
        );

        assertEquals("All AI services failed", exception.getMessage());
        assertEquals(clientException, exception.getCause());

        verify(firstClient, times(1)).callAI(testMessages, testFunctionDefinition);
    }
}
