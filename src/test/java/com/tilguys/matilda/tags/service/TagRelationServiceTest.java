package com.tilguys.matilda.tags.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.tilguys.matilda.tag.domain.SubTag;
import com.tilguys.matilda.tag.repository.SubTagRepository;
import com.tilguys.matilda.tag.repository.TagRelationRepository;
import com.tilguys.matilda.tag.repository.TagRepository;
import com.tilguys.matilda.tag.service.TagRelationService;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilCreateRequest;
import com.tilguys.matilda.til.service.TilService;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TagRelationServiceTest {

    @Autowired
    private TagRelationService tagRelationService;
    @Autowired
    private TagRelationRepository tagRelationRepository;
    @Autowired
    private SubTagRepository subTagRepository;
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TilService tilService;

    @Test
    void 주어진_태그들로_태그_관계를_업데이트할_수_있다() {
        TilUser tilUser = userRepository.save(new TilUser(null, ProviderInfo.GITHUB, "tmp", Role.USER, "asdf", "asdf"));
        Til til = tilService.createTil(new TilCreateRequest("title", "content", LocalDate.now(), true),
                tilUser.getId());

        Tag aTag = new Tag(null, "A", til);
        Tag bTag = new Tag(null, "B", til);
        Tag cTag = new Tag(null, "C", til);
        List<Tag> tags = List.of(aTag, bTag, cTag);
        til.updateTags(tags);

        SubTag aSubTag = new SubTag(null, "ASubTag", aTag);
        SubTag bSubTag = new SubTag(null, "BSubTag", bTag);
        SubTag cSubTag = new SubTag(null, "CSubTag", cTag);

        List<SubTag> subTags = List.of(aSubTag, bSubTag, cSubTag);
        tagRepository.saveAll(tags);
        subTagRepository.saveAll(subTags);

        long before = tagRelationRepository.count();
        tagRelationService.updateCoreTagsRelation();

        assertThat(tagRelationRepository.count()).isNotEqualTo(before);
    }

    @Test
    void 최근_태그관계들을_가져올_수_있다() {
        TilUser tilUser = userRepository.save(new TilUser(null, ProviderInfo.GITHUB, "tmp", Role.USER, "asdf", "asdf"));
        Til til = tilService.createTil(new TilCreateRequest("title", "content", LocalDate.now(), true),
                tilUser.getId());

        Tag aTag = new Tag(null, "A", til);
        Tag bTag = new Tag(null, "B", til);
        Tag cTag = new Tag(null, "C", til);
        List<Tag> tags = List.of(aTag, bTag, cTag);
        til.updateTags(tags);

        SubTag aSubTag = new SubTag(null, "ASubTag", aTag);
        SubTag bSubTag = new SubTag(null, "BSubTag", bTag);
        SubTag cSubTag = new SubTag(null, "CSubTag", cTag);

        List<SubTag> subTags = List.of(aSubTag, bSubTag, cSubTag);
        tagRepository.saveAll(tags);
        subTagRepository.saveAll(subTags);

        tagRelationService.updateCoreTagsRelation();

        assertThat(tagRelationService.getRecentRelationTagMap().keySet().size()).isEqualTo(tags.size());
    }

    @Test
    void 삭제된_TIL의_관계들은_가져오지_않는다() {
        TilUser tilUser = userRepository.save(new TilUser(null, ProviderInfo.GITHUB, "tmp", Role.USER, "asdf", "asdf"));
        Til til = tilService.createTil(new TilCreateRequest("title", "content", LocalDate.now(), true),
                tilUser.getId());

        Tag aTag = new Tag(null, "A", til);
        Tag bTag = new Tag(null, "B", til);
        Tag cTag = new Tag(null, "C", til);
        List<Tag> tags = List.of(aTag, bTag, cTag);

        SubTag aSubTag = new SubTag(null, "ASubTag", aTag);
        SubTag bSubTag = new SubTag(null, "BSubTag", bTag);
        SubTag cSubTag = new SubTag(null, "CSubTag", cTag);

        List<SubTag> subTags = List.of(aSubTag, bSubTag, cSubTag);
        tagRepository.saveAll(tags);
        subTagRepository.saveAll(subTags);

        tagRelationService.updateCoreTagsRelation();

        tilService.deleteTil(til.getTilId());
        Map<Tag, List<Tag>> recentRelationTagMap = tagRelationService.getRecentRelationTagMap();
        assertThat(recentRelationTagMap.keySet().size()).isEqualTo(0L);
    }
}
