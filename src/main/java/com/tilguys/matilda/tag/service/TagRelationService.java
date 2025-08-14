package com.tilguys.matilda.tag.service;

import com.tilguys.matilda.tag.domain.TagRelation;
import com.tilguys.matilda.tag.repository.TagRelationRepository;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.service.TilService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TagRelationService {

    private static final Long TAG_RELATION_RENEW_PERIOD = 500L;

    private final TagRelationRepository tagRelationRepository;
    private final TilService tilService;

    public TagRelationService(TagRelationRepository tagRelationRepository, TilService tilService) {
        this.tagRelationRepository = tagRelationRepository;
        this.tilService = tilService;
    }

    @Transactional
    public void renewCoreTagsRelation() {
        tagRelationRepository.deleteAll();
        LocalDateTime startDateTime = LocalDate.now()
                .minusDays(TAG_RELATION_RENEW_PERIOD)
                .atStartOfDay();
        List<Til> recentWroteTil = tilService.getRecentWroteTil(startDateTime);

        for (Til til : recentWroteTil) {
            List<Tag> tags = til.getTags();
            createTagsRelation(tags);
        }
    }

    private void createTagsRelation(List<Tag> recentWroteTags) {
        for (int i = 0; i < recentWroteTags.size(); i++) {
            for (int j = 0; j < recentWroteTags.size(); j++) {
                if (i == j) {
                    continue;
                }
                tagRelationRepository.save(new TagRelation(null, recentWroteTags.get(i), recentWroteTags.get(j)));
            }
        }
    }

    public Map<Tag, List<Tag>> getRecentRelationTagMap() {
        LocalDateTime startDateTime = LocalDate.now()
                .minusDays(TAG_RELATION_RENEW_PERIOD)
                .atStartOfDay();
        List<TagRelation> tagRelations = tagRelationRepository.findByCreatedAtGreaterThanEqual(startDateTime);

        Map<Tag, List<Tag>> tagMap = new HashMap<>();
        for (TagRelation tagRelation : tagRelations) {
            if (!tagRelation.getTag()
                    .getTil()
                    .isNotDeleted()) {
                continue;
            }

            List<Tag> relationTags = tagMap.getOrDefault(tagRelation.getTag(), new ArrayList<>());
            relationTags.add(tagRelation.getOtherTag());
            tagMap.put(tagRelation.getTag(), relationTags);
        }
        return tagMap;
    }
}
