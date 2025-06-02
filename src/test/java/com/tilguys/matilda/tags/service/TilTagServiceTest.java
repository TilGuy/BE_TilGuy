package com.tilguys.matilda.tags.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.tilguys.matilda.common.external.OpenAIClient;
import com.tilguys.matilda.tag.repository.TagRepository;
import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import({TilTagService.class, OpenAIClient.class})
class TilTagServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TilTagService tilTagService;

    @Test
    void 지정한_날짜로_태그들을_가져올_수_있다() {
        Til til = Til.builder()
                .content("asdf")
                .date(LocalDate.now())
                .isDeleted(false)
                .isPublic(false)
                .tags(new ArrayList<>())
                .tilUser(null)
                .title("asdf")
                .build();
        Tag tag = new Tag(null, "Asdf", til);

        til.updateTags(List.of(tag));
        em.persist(til);

        assertThat(tagRepository.findAll().size()).isEqualTo(1L);
        LocalDate startDay = LocalDate.now().minusDays(7L);

        assertThat(tilTagService.getRecentWroteTags(startDay).size()).isEqualTo(1L);
    }
}
