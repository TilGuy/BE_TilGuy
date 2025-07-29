package com.tilguys.matilda.common.external;

import java.util.List;
import java.util.Map;

public interface AIClient {
    String callAI(List<Map<String, Object>> messages, Map<String, Object> functionDefinition);
    String getClientName();
    boolean isAvailable();
} 