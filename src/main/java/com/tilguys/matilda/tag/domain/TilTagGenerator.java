package com.tilguys.matilda.tag.domain;

import java.util.List;
import java.util.Map;

public class TilTagGenerator {

    private static final String STRING_TYPE = "string";
    private static final String DESCRIPTION = "description";
    private static final String TAG_GENERATE_PROMPT = """            
            [TIL 시작]
            %s
            [TIL 끝]
            
            위 TIL 내용을 기반으로 AI는 아래의 세 작업을 수행해야 합니다:
            A. 아래 '핵심 태그 후보들' 중에서 TIL과 연관성이 높은 핵심 태그를 최대 5개 추출해 주세요. **연관성 없는 태그는 제외**하며, 반드시 아래 목록에서만 선택해 주세요.
            핵심 태그 후보:
            자바,스프링,JPA,데이터베이스,MySQL,테스트,서버 관리,https,ci/cd,aws,모니터링,로깅,객체지향,디자인패턴,리팩터링,클린코드,시스템아키텍쳐,동시성,성능최적화,확장성,깃,코드리뷰,페어프로그래밍,팀프로젝트,애자일,api,설계,문서화,자료구조,알고리즘,네트워크,운영체제,소프트스킬,회고,학습법,인프라,보안
            B. 위에서 추출한 각 핵심 태그에 대해 TIL과 관련된 서브 태그를 최대 5개씩 생성해 주세요. **실제 TIL 내용과 연관성이 있어야 하며, 의미 없는 일반 키워드는 제외**합니다.
            C. A와 B에서 절대로 연관성,의미없는 태그를 생성하지마십시오.""";

    public List<Map<String, Object>> createPrompt(String tilContent) {

        return List.of(
                Map.of("role", "system", "content", "당신은 TIL 내용을 분석하여 관련 태그를 추출하고, 태그별로 서브태그를 만드는 전문가입니다."),
                Map.of("role", "user", "content", TAG_GENERATE_PROMPT.formatted(tilContent))
        );
    }

    public Map<String, Object> createFunctionDefinition() {

        Map<String, Object> parameters = Map.of(
                "type", "object",
                "properties", Map.of(
                        "tags", Map.of(
                                "type", "array",
                                DESCRIPTION, "Til 내용을 분석한뒤 분류된 핵심 태그들 (TIL 내용과 연관성 있는것만 포함할것)",
                                "items", Map.of("type", STRING_TYPE)
                        ),
                        "subTags", Map.of(
                                "type", "array",
                                DESCRIPTION, "ai가 생성했던 tags에 대한 서브태그들",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "coreTag", Map.of(
                                                        "type", STRING_TYPE,
                                                        DESCRIPTION,
                                                        "서브 태그가 속한 핵심 태그. 핵심태그는 요청 A에 의한 ai가 생성한 태그들임(프로퍼티상 tags라는 키)"
                                                ),
                                                "subTag", Map.of(
                                                        "type", STRING_TYPE,
                                                        DESCRIPTION, "서브 태그(TIL 내용과 연관성 있는것만 포함할것)"
                                                )
                                        ),
                                        "required", List.of("coreTag", "subTag")
                                )
                        )
                ),
                "required", List.of("tags", "subTags")
        );

        return Map.of(
                "name", "extractTilTags",
                DESCRIPTION, "TIL 내용에서 관련 핵심,서브태그들을 추출합니다.",
                "parameters", parameters
        );
    }
}
