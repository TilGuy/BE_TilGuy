package com.tilguys.matilda.reference.domain;

import java.util.List;
import java.util.Map;

public class TilReferenceGenerator {

    public List<Map<String, Object>> createPrompt(String tilContent) {
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content",
                "다음 TIL 내용을 분석하여 독자가 이해하기 어려울 수 있는 핵심 기술 용어나 개념을 추출하고, " +
                        "각각에 대한 간단한 설명이나 참고 자료를 제공해주세요. " +
                        "CS 전문 용어, 라이브러리명, 프레임워크, 알고리즘 등이 포함될 수 있습니다. " +
                        "글의 난이도를 토대로 관련 분야 입문자가 대상이라 판단하되, 억지로 선별할 필요는 없습니다." +
                        "최대 5개까지 선별해주세요. " +
                        "각 항목은 용어와 해당 용어에 대한 간단한 설명 또는 공식 문서 링크를 포함해야 합니다: " +
                        tilContent
        );

        return List.of(userMessage);
    }

    public Map<String, Object> createFunctionDefinition() {
        // 참조 객체의 구조를 정의합니다
        Map<String, Object> referenceObject = Map.of(
                "type", "object",
                "properties", Map.of(
                        "word", Map.of(
                                "type", "string",
                                "description", "TIL에서 추출한 핵심 기술 용어나 개념"
                        ),
                        "reference", Map.of(
                                "type", "string",
                                "description", "해당 용어에 대한 간단한 설명, 정의, 또는 참고할 만한 공식 문서나 학습 자료의 정보"
                        )
                ),
                "required", List.of("word", "reference")
        );

        Map<String, Object> parameters = Map.of(
                "type", "object",
                "properties", Map.of(
                        "references", Map.of(
                                "type", "array",
                                "description", "TIL에서 추출한 핵심 용어와 참조 정보의 목록",
                                "items", referenceObject
                        )
                ),
                "required", List.of("references")
        );

        return Map.of(
                "name", "extractTilReferences",
                "description", "TIL 내용에서 핵심 기술 용어를 추출하고 각각에 대한 참조 정보를 제공합니다.",
                "parameters", parameters
        );
    }
}
