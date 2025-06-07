package com.tilguys.matilda.tag.service;

import com.tilguys.matilda.tag.domain.TagRelation;
import com.tilguys.matilda.tag.repository.TagRelationRepository;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.service.TilService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagRelationService {

    private static final Long TAG_GET_START_DAY = 7L;

    private final TagRelationRepository tagRelationRepository;
    private final TilService tilService;

    public TagRelationService(TagRelationRepository tagRelationRepository, TilService tilService) {
        this.tagRelationRepository = tagRelationRepository;
        this.tilService = tilService;
    }

    @Transactional
    public void updateCoreTagsRelation() {
        LocalDateTime startDateTime = LocalDate.now().minusDays(TAG_GET_START_DAY).atStartOfDay();
        List<Til> recentWroteTil = tilService.getRecentWroteTil(startDateTime);

        for (Til til : recentWroteTil) {
            List<Tag> tags = til.getTags();
            createTagsRelation(tags);
        }
    }

    private void createTagsRelation(List<Tag> recentWroteTags) {
        for (int i = 0; i < recentWroteTags.size(); i++) {
            for (int j = i + 1; j < recentWroteTags.size(); j++) {
                tagRelationRepository.save(new TagRelation(null, recentWroteTags.get(i), recentWroteTags.get(j)));
            }
        }
    }

    public Map<Tag, List<Tag>> getRecentRelationTagMap() {
        LocalDateTime startDateTime = LocalDate.now().minusDays(TAG_GET_START_DAY).atStartOfDay();
        List<TagRelation> tagRelations = tagRelationRepository.findByCreatedAtGreaterThanEqual(startDateTime);

        Map<Tag, List<Tag>> tagMap = new HashMap<>();
        for (TagRelation tagRelation : tagRelations) {
            List<Tag> relationTags = tagMap.getOrDefault(tagRelation.getTag(), new ArrayList<>());
            relationTags.add(tagRelation.getOtherTag());
            tagMap.put(tagRelation.getTag(), relationTags);
        }
        return tagMap;
    }
}
