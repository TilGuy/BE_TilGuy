package com.tilguys.matilda.common.external;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class AIServiceManager {

    private static final Logger log = LoggerFactory.getLogger(AIServiceManager.class);

    private final List<AIClient> aiClients;
    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public AIServiceManager(List<AIClient> aiClients) {
        this.aiClients = aiClients.stream()
                .filter(AIClient::isAvailable)
                .toList();

        initializeCircuitBreakers();
        log.info(
                "Available AI Clients: {}",
                this.aiClients.stream()
                        .map(AIClient::getClientName)
                        .toList()
        );
    }

    public String callAIWithFallback(
            List<Map<String, Object>> messages,
            Map<String, Object> functionDefinition
    ) {
        for (AIClient client : aiClients) {
            try {
                CircuitBreaker circuitBreaker = circuitBreakers.get(client.getClientName());

                Supplier<String> decoratedSupplier = CircuitBreaker
                        .decorateSupplier(circuitBreaker, () -> client.callAI(messages, functionDefinition));

                String result = decoratedSupplier.get();
                log.info("Successfully called {} API", client.getClientName());
                return result;

            } catch (Exception e) {
                log.warn("Failed to call {} API: {}", client.getClientName(), e.getMessage());
                if (isLastClient(client)) {
                    throw new RuntimeException("All AI services failed", e);
                }
            }
        }

        throw new RuntimeException("No available AI services");
    }

    private void initializeCircuitBreakers() {
        // 기본 설정: 안정성 중심 (기존)
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% 실패율에서 서킷 오픈
                .waitDurationInOpenState(Duration.ofSeconds(30)) // 30초 대기
                .slidingWindowSize(5) // 최근 5회 호출 기준
                .minimumNumberOfCalls(3) // 최소 3회 호출 후 판단
                .build();
        
        // 빠른 실패 설정: 1회 실패 시 즉시 오픈
        CircuitBreakerConfig fastFailConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(1) // 1% 실패율에서도 서킷 오픈  
                .waitDurationInOpenState(Duration.ofSeconds(10)) // 10초 대기
                .slidingWindowSize(1) // 최근 1회 호출만 고려
                .minimumNumberOfCalls(1) // 1회 호출 후 즉시 판단
                .build();

        for (AIClient client : aiClients) {
            // OpenAI는 안정성 중심, 다른 API는 빠른 실패 적용
            CircuitBreakerConfig config = "OpenAI".equals(client.getClientName()) 
                    ? defaultConfig : fastFailConfig;
            
            CircuitBreaker circuitBreaker = CircuitBreaker.of(client.getClientName(), config);
            circuitBreaker.getEventPublisher()
                    .onStateTransition(event ->
                            log.info(
                                    "Circuit breaker {} state transition: {} -> {}",
                                    client.getClientName(),
                                    event.getStateTransition()
                                            .getFromState(),
                                    event.getStateTransition()
                                            .getToState()
                            ));

            circuitBreakers.put(client.getClientName(), circuitBreaker);
        }
    }

    private boolean isLastClient(AIClient client) {
        return aiClients.indexOf(client) == aiClients.size() - 1;
    }

    public List<String> getAvailableClients() {
        return aiClients.stream()
                .map(AIClient::getClientName)
                .toList();
    }

    public Map<String, String> getCircuitBreakerStates() {
        return circuitBreakers.entrySet()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()
                                .getState()
                                .toString()
                ));
    }
}
