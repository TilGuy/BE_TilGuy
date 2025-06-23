package com.tilguys.matilda.reference.domain;

import java.util.List;
import java.util.Map;

public class TilReferenceGenerator {

    private static final String DESCRIPTION = "description";

    public List<Map<String, Object>> createPrompt(String tilContent) {
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content",
                """
                        다음 TIL 내용을 분석하여 독자가 이해하기 어려울 수 있는 핵심 기술 용어나 개념을 추출하고, 
                        각각에 대한 간단한 설명을 제공해주세요. 
                        IT 관련 분야 중급자 이상 대상이라 판단하여, CS 전문 용어, 라이브러리명, 프레임워크, 알고리즘 등이 포함될 수 있습니다. 
                        CS 분야와 관련 없는 내용은 절대 추출하지 않습니다.
                        최대 5개까지 선별해주세요. 
                        각 항목은 용어와 해당 용어에 대한 간단한 설명을 포함해야 합니다. 
                        설명은 반드시 한국어로 작성해야합니다: 
                        """ + "\n" + tilContent
        );
        return List.of(userMessage);
    }

    public Map<String, Object> createFunctionDefinition() {
        Map<String, Object> referenceObject = Map.of(
                "type", "object",
                "properties", Map.of(
                        "word", Map.of(
                                "type", "string",
                                DESCRIPTION, "TIL에서 추출한 핵심 기술 용어나 개념"
                        ),
                        "info", Map.of(
                                "type", "string",
                                DESCRIPTION, "해당 용어에 대한 간단한 설명, 정의, 또는 참고할 만한 공식 문서나 학습 자료의 정보"
                        )
                ),
                "required", List.of("word", "info")
        );

        Map<String, Object> parameters = Map.of(
                "type", "object",
                "properties", Map.of(
                        "references", Map.of(
                                "type", "array",
                                DESCRIPTION, "TIL에서 추출한 핵심 용어와 참조 정보의 목록",
                                "items", referenceObject
                        )
                ),
                "required", List.of("references")
        );

        return Map.of(
                "name", "extractTilReferences",
                DESCRIPTION, "TIL 내용에서 핵심 기술 용어를 추출하고 각각에 대한 참조 정보를 제공합니다.",
                "parameters", parameters
        );
    }
}
