package com.tilguys.matilda.tag.domain;

import java.util.List;
import java.util.Map;

public class TilTagGenerator {

    public List<Map<String, Object>> createPrompt(String tilContent) {
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", "다음의 TIL을 내용을 기반으로 TIL의 기술 태그들을 추출해주세요: " + tilContent
        );

        return List.of(userMessage);
    }

    public Map<String, Object> createFunctionDefinition() {
        Map<String, Object> parameters = Map.of(
                "type", "object",
                "properties", Map.of(
                        "tags", Map.of(
                                "type", "array",
                                "description", "TIL에서 추출한 기술 태그 목록",
                                "items", Map.of(
                                        "type", "string"
                                )
                        )
                ),
                "required", List.of("tags")
        );

        return Map.of(
                "name", "extractTilTags",
                "description", "TIL 내용에서 기술 관련 태그들을 추출합니다.",
                "parameters", parameters
        );
    }
}
