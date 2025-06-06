package com.tilguys.matilda.study.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.tilguys.matilda.tag.service.TilTagService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@ActiveProfiles("test")
@Disabled
class TilTagServiceTest {

    @Autowired
    private TilTagService tilTagService;

    @Test
    void gpt_실제_할당량_소모_요청_테스트() {
        String tilContent = "## `DTO` 계층 분리 트레이드 오프\n"
                + "                \"1.다음의 TIL을 내용을 기반으로 TIL 내용에서 핵심으로 생각되는 태그들을 추출해주세요. 2.억지로 채우는것보다 핵심적인 내용만 채우는게 중요하고 최소 3개입니다: 4. (최대 5개) 다음의 태그들 중에서 가장 연관성이 높은것들로 태그를 분류해야합니다. 태그들: 자바,스프링부트,JPA,데이터베이스,MySQL,테스트,서버 관리, https,ci/cd,aws,모니터링,로깅,객체지향,디자인패턴,리팩터링,클린코드,시스템아키텍쳐,동시성,성능최적화,확장성,깃,코드리뷰,페어프로그래밍,팀프로젝트,애자일,api,설계,문서화,자료구조,알고리즘,네트워크,운영체제,소프트스킬,회고,학습법 \n"
                + "B. 서브태그들을 추가해주세요 서브태그는 핵심 태그들에 대한 세부 태그들입니다. ex)스프링 - di과 같은 방식 최대한 TIL에서 뽑아낼 수 있는 최대 5개 뽑아내야함.\""
                + ""
                + "- 계층 분리 방법 목록\n"
                + "    - `Presentation`\n"
                + "    - `Application`\n"
                + "    - DTO 독립 배치\n"
                + "- 여러 방향에서 `Application` 계층이 가장 단점 최소\n"
                + "    - 개발 비용\n"
                + "    - 유지 보수\n"
                + "    - `요청` - `Controller` - `Service` 까지 어디에 배치해도 포함 가능한 구조\n"
                + "\n"
                + "## `Mockito` 사용\n"
                + "- 참다가 결국 사용, 바로 리뷰어(제이미) 질문 ON\n"
                + "- 사용한 이유 답변\n"
                + "    - Fake 객체 생성 의무가 없어 개발 비용이 감소\n"
                + "    - 테스트 메서드에서 `when`, `retrun` 을 통해 명확한 조건과 결과로 **문서화 효과**\n"
                + "    - 다른 테스트와의 의존 없이 독립적인 테스트 가능\n"
                + "    - 상황에 따라 다양한 값을 설정해 테스트를 수행할 수 있어, **유연성과 확장성** \uD83D\uDC4D\n"
                + "    - `verify()` 메서드를 통해 로직 상 메서드 호출 여부와 횟수까지 검증할 수 있어, 서비스 내부 흐름까지 테스트 가능\n"
                + "\n"
                + "## `디미터 법칙` 무분별한 적용은 악\n"
                + "- 디미터 법칙을 준수하면, 응집도는 높아지고 결합도는 낮아질 수 있다.\n"
                + "- 하지만, 무분별한 분리는 **객체 역할의 모호함이 발생**할 수 있다.\n"
                + "- 이와 같은 문제점을 잘 생각해 객체의 역할에 집중해야 한다.\n"
                + "- 또한, 무분별한 분리는 오히려, **SRP 원칙을 해칠 수 있다.**\n"
                + "- 적절한 `Getter` 사용은 옳다.\n"
                + "    -  상태 타입이 객체냐 자료 구조이냐 다르다.";
        assertThat(tilTagService.extractTilTags(tilContent)).hasSizeGreaterThan(0);
    }
}
