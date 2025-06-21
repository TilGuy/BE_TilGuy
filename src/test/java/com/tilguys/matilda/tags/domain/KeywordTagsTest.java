package com.tilguys.matilda.tags.domain;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.tilguys.matilda.tag.domain.KeywordTags;
import com.tilguys.matilda.tag.domain.SubTag;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@ActiveProfiles("test")
class KeywordTagsTest {

    @Test
    void 태그_정보들로_keyword_tags를_생성할_수_있다() {
        // Given
        TilUser tilUser = new TilUser(1L, ProviderInfo.GITHUB, "asdf", Role.USER, "praise", "asd");
        Til til = new Til(10L, tilUser, "title", "content", LocalDate.now(), true, false, new ArrayList<>(),
                new ArrayList<>());
        Tag tagA = new Tag(100L, "A", til);
        Tag tagB = new Tag(101L, "B", til);
        List<Tag> tags = List.of(tagA, tagB);

        SubTag subTagA1 = new SubTag(200L, "A-1", tagA);
        SubTag subTagA2 = new SubTag(201L, "A-2", tagA);
        SubTag subTagB1 = new SubTag(202L, "B-1", tagB);
        List<SubTag> subTags = List.of(subTagA1, subTagA2, subTagB1);

        Map<Tag, List<Tag>> tagRelationMap = new HashMap<>();
        tagRelationMap.put(tagA, List.of(tagB));
        tagRelationMap.put(tagB, List.of(tagA));

        // When
        KeywordTags keywordTags = new KeywordTags(tags, subTags, tagRelationMap);

        // Then
        // keywordTagMap 검증
        assertThat(keywordTags.getKeywordTagMap().get("A")).containsExactlyInAnyOrder("A-1", "A-2");
        assertThat(keywordTags.getKeywordTagMap().get("B")).containsExactlyInAnyOrder("B-1");

        // tagRelationMap 검증
        assertThat(keywordTags.getTagRelationMap().get("A")).containsExactly("B");
        assertThat(keywordTags.getTagRelationMap().get("B")).containsExactly("A");

        // tagTilIdMap 검증
        assertThat(keywordTags.getTagTilIdMap().get("A")).containsExactly(10L);
        assertThat(keywordTags.getTagTilIdMap().get("B")).containsExactly(10L);
    }
}
