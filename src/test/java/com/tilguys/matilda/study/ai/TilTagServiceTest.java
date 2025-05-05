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
//할당량을 소모하니 필요할때만 테스트할것!
class TilTagServiceTest {

    @Autowired
    private TilTagService tilTagService;

    @Test
    void gpt_실제_할당량_소모_요청_테스트() {
        String tilContent = "DTO를 사용하는 이유\n"
                + "객체 자체를 넘겨주게 되면 불필요한 정보까지 넘어가게 된다.\n"
                + "예시로, User라는 객체에 password 필드가 있다고 하자. 요청에 이것까지 넘겨주면 큰 일이 날 수 있다.\n"
                + "모델과 뷰 사이에 의존성이 생긴다. 이 의존성을 끊기 위해 사용한다.\n"
                + "왜 의존성을 끊으려 하나 ?\n"
                + "예시로, 필드값이 바뀌면 반환하는 api 명세서도 바뀌게 된다. 확장성이 고려되지 않는다.\n"
                + "유저라는 객체가 있다고 하자.\n"
                + "기존에는 전화번호, 이름 필드만 존재했다. 이를 이용하여 유저 자체를 응답으로 내보냈었다.\n"
                + "갑자기 요구사항의 변경으로 인해 이메일이 필드에 추가됐다. 그랬더니 전화번호와 이름을 응답으로 내보내는 api 스펙에 이메일도 추가가 되어버렸다.\n"
                + "만약, DTO로 관리했더라면 응답 스펙이 바뀌지 않았을 것이고, 추가만 했으면 됐다.\n"
                + "그렇다면 클라이언트에게 노출해도 되는 값만 가진 도메인 객체라면 그대로 리턴해도 되는걸까?\n"
                + "관심사의 분리\n"
                + "우리가 mvc 패턴을 사용하는 이유는 무엇인가 ? → 유지보수\n"
                + "만약 도메인을 내보낸다면 두 계층이 섞여버린다. (프레젠테이션, 비즈니스)\n"
                + "왜냐, 프레젠테이션에도 해당 도메인이 쓰이고, 비즈니스 로직에도 도메인이 쓰이기 때문이다.\n"
                + "결과적으로 요구사항이 변경을 생각하는 것뿐만 아니라, 레이어의 분리를 위해 나눈다.\n"
                + "네트워크 비용을 줄일 수 있다.\n"
                + "예시로, 유저 테이블이 있고 게시판, 상품 판매, 결제 Q&A가 있다고 해보자.\n"
                + "상품 정보와 QNA는 서로 다른 테이블이고, 특정 상품 소개 페이지에 관련있는 질문들의 QNA를 뽑아서 보여주고 싶고, 구매한 유저의 정보까지 포함시키고 싶다.\n"
                + "그렇다면 총 3번의 API 통신을 거쳐야 한다.\n"
                + "상품 소개 API\n"
                + "QNA API\n"
                + "USER API\n"
                + "DTO를 쓴다면, 1번의 API 통신으로 원하는 정보를 모두 얻을 수 있다.\n"
                + "API 스펙을 명확하게 정할 수 있다.\n"
                + "도메인은 null이 들어갈 수도 있고, 어떠한 경우에는 값이 있을 수 있다.\n"
                + "만약 중요한 필드이면 유지보수가 어렵다.\n"
                + "null에 대한 이유가 없기 때문이다. null이 들어오는 것이 문제가 되진 않지만, 이게 null인 이유는 알아야 한다.(DB오류, 사용자가 적지 않음 등 많은 이유가 있을 수 있다.)\n"
                + "API 응답 스펙이 정해진다면 그 필드의 값은 항상 같은 원칙으로 반환되도록 명확하게 설계가 가능하다.";
        assertThat(tilTagService.extractTilTags(tilContent)).hasSizeGreaterThan(0);
    }
}
