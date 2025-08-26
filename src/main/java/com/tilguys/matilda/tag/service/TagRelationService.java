package com.tilguys.matilda.tag.service;

import com.tilguys.matilda.tag.domain.TagRelation;
import com.tilguys.matilda.tag.repository.TagRelationRepository;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.service.TilService;
import jakarta.persistence.EntityManager;
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
    private static final int BATCH_SAVE_SIZE = 1000;

    private final TagRelationRepository tagRelationRepository;
    private final TilService tilService;
    private final EntityManager entityManager;

    public TagRelationService(
            TagRelationRepository tagRelationRepository,
            TilService tilService,
            EntityManager entityManager
    ) {
        this.tagRelationRepository = tagRelationRepository;
        this.tilService = tilService;
        this.entityManager = entityManager;
    }

    @Transactional
    public void renewCoreTagsRelation() {
        tagRelationRepository.deleteAllInBatch();

        LocalDateTime startDateTime = LocalDate.now()
                .minusDays(TAG_RELATION_RENEW_PERIOD)
                .atStartOfDay();

        List<Til> recentWroteTil = tilService.getRecentWroteTil(startDateTime);

        List<TagRelation> tilRelations = new ArrayList<>();

        for (Til til : recentWroteTil) {
            List<Tag> tags = til.getTags();

            tilRelations.addAll(createRelatedTag(tags));

            if (tilRelations.size() >= BATCH_SAVE_SIZE) {
                tagRelationRepository.saveAll(tilRelations);
                tilRelations.clear();
                entityManager.flush();
                entityManager.clear();
            }
        }

        if (!tilRelations.isEmpty()) {
            tagRelationRepository.saveAll(tilRelations);
            entityManager.flush();
            entityManager.clear();
        }
    }

    private List<TagRelation> createRelatedTag(List<Tag> tags) {
        List<TagRelation> tagRelations = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            for (int j = 0; j < tags.size(); j++) {
                if (i == j) {
                    continue;
                }
                tagRelations.add(new TagRelation(null, tags.get(i), tags.get(j)));
            }
        }
        return tagRelations;
    }

    @Transactional(readOnly = true)
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
