package com.tilguys.matilda.tag.domain;

import java.util.List;
import java.util.Map;

public class TilTagGenerator {

    public List<Map<String, Object>> createPrompt(String tilContent) {
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content",
                "첫번째요청 A.C에서 제공될 Til의 내용을 분석한뒤 다음의 '핵심 태그 후보들' 중에서 가장 해당 Til과 연관성이 높은 '핵심 태그'를 5개 찾아주세요.(최대 5개 - 무조건 5개를 채워야하는건아님.) 반드시 핵심 태그 후보들 중에서 선택해야합니다.\n"
                        + " '핵심 태그 후보들': 자바,스프링,JPA,데이터베이스,MySQL,테스트,서버 관리, https,ci/cd,aws,모니터링,로깅,객체지향,디자인패턴,리팩터링,클린코드,시스템아키텍쳐,동시성,성능최적화,확장성,깃,코드리뷰,페어프로그래밍,팀프로젝트,애자일,api,설계,문서화,자료구조,알고리즘,네트워크,운영체제,소프트스킬,회고,학습법,인프라,보안\n"
                        + " 두번째요청 : 서브태그생성. 당신이 분류해낸뒤의 '핵심 태그'에 대해서 좀 더 세부적인 키워드를 가진 태그를 최대 5개씩 생성해주세요. \n C: Til내용:"
                        + tilContent
        );

        return List.of(userMessage);
    }

    public Map<String, Object> createFunctionDefinition() {
        Map<String, Object> parameters = Map.of(
                "type", "object",
                "properties", Map.of(
                        "tags", Map.of(
                                "type", "array",
                                "description", "Til 내용을 분석한뒤 분류된 핵심 태그들",
                                "items", Map.of("type", "string")
                        ),
                        "subTags", Map.of(
                                "type", "array",
                                "description", "핵심 태그에 대한 서브태그들",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "coreTag", Map.of(
                                                        "type", "string",
                                                        "description", "서브 태그가 속한 핵심 태그. 핵심태그는 A요청에서 분류된 태그여야함"
                                                ),
                                                "subTag", Map.of(
                                                        "type", "string",
                                                        "description", "서브 기술 태그"
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
                "description", "TIL 내용에서 관련 핵심,서브태그들을 추출합니다.",
                "parameters", parameters
        );
    }
}
