package com.tilguys.matilda.study.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.tilguys.matilda.reference.service.TilReferenceService;
import com.tilguys.matilda.til.domain.Reference;
import java.util.List;
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
class TilReferenceServiceTest {

    @Autowired
    private TilReferenceService tilReferenceService;

    @Test
    void gpt_출력_결과_테스트() {
        String tilContent = "## `DTO` 계층 분리 트레이드 오프\n"
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
        List<Reference> actual = tilReferenceService.extractTilReference(tilContent);
        assertThat(actual).hasSizeGreaterThan(0);
        System.out.println(
                "GPT가 추출한 단어: " + actual.stream().map(Reference::getWord).toList()
        );
        System.out.println(
                "GPT가 추출한 레퍼런스: " + actual.stream().map(Reference::getReference).toList()
        );
    }
}
