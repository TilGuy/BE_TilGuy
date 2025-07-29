package com.tilguys.matilda.common.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FailoverAIServiceManager {

    private static final Logger log = LoggerFactory.getLogger(FailoverAIServiceManager.class);

    private final List<AIClient> aiClients;

    public FailoverAIServiceManager(List<AIClient> aiClients) {
        this.aiClients = aiClients;

        log.info(
                "Available AI Clients: {}",
                this.aiClients.stream()
                        .map(AIClient::getClientName)
                        .toList()
        );
    }

    public String callAIWithSimpleFallback(
            List<Map<String, Object>> messages,
            Map<String, Object> functionDefinition
    ) {
        for (AIClient client : aiClients) {
            try {
                log.info("Attempting to call {} API", client.getClientName());
                String result = client.callAI(messages, functionDefinition);
                log.info("Successfully called {} API", client.getClientName());
                return result;

            } catch (Exception e) {
                log.warn("Failed to call {} API: {}", client.getClientName(), e.getMessage());
                if (isLastClient(client)) {
                    throw new RuntimeException("All AI services failed", e);
                }
                log.info("Trying next AI service");
            }
        }

        throw new RuntimeException("No available AI services");
    }

    private boolean isLastClient(AIClient client) {
        return aiClients.indexOf(client) == aiClients.size() - 1;
    }
}
