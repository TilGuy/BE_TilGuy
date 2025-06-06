package com.tilguys.matilda.tag.domain;

import java.util.List;
import java.util.Map;

public class TilTagGenerator {

    public List<Map<String, Object>> createPrompt(String tilContent) {
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content",
                "1.다음의 TIL을 내용을 기반으로 TIL 내용에서 핵심으로 생각되는 태그들을 추출해주세요. 2.억지로 채우는것보다 핵심적인 내용만 채우는게 중요하고 최소 3개입니다: 4. (최대 5개) 다음의 태그들 중에서 가장 연관성이 높은것들로 태그를 분류해야합니다. 태그들: 자바,스프링,JPA,데이터베이스,MySQL,테스트,서버 관리, https,ci/cd,aws,모니터링,로깅,객체지향,디자인패턴,리팩터링,클린코드,시스템아키텍쳐,동시성,성능최적화,확장성,깃,코드리뷰,페어프로그래밍,팀프로젝트,애자일,api,설계,문서화,자료구조,알고리즘,네트워크,운영체제,소프트스킬,회고,학습법 B. 서브태그들을 추가해주세요 서브태그는 이전 핵심 태그들과 별개로 최대한 TIL에서 뽑아낼 수 있는 태그를 최대 5개 뽑아내야합니다."
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
