package com.tilguys.matilda.tag.schedule;


import com.tilguys.matilda.tag.cache.RecentTilTagsCache;
import com.tilguys.matilda.tag.domain.SubTag;
import com.tilguys.matilda.tag.domain.TilTagRelations;
import com.tilguys.matilda.tag.service.TagRelationService;
import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.domain.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@ConditionalOnProperty(
        name = "matilda.cache.tag.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class TagScheduledJob {

    private static final int TAG_GET_START_DAY = 500;

    private final TagRelationService tagRelationService;
    private final TilTagService tilTagService;
    private final RecentTilTagsCache recentTilTagsCache;

    public TagScheduledJob(
            TagRelationService tagRelationService, TilTagService tilTagService,
            RecentTilTagsCache recentTilTagsCache
    ) {
        this.tagRelationService = tagRelationService;
        this.tilTagService = tilTagService;
        this.recentTilTagsCache = recentTilTagsCache;
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void updateRecentTagRelations() {
        log.info("recent tag 관계 캐싱 시작!");
        tagRelationService.renewCoreTagsRelation();
        TilTagRelations recentTagRelations = createRecentTagRelations();
        recentTilTagsCache.updateRecentTagRelations(recentTagRelations);
        log.info("recent tag 관계 캐싱 완료");
    }

    private TilTagRelations createRecentTagRelations() {
        LocalDate startDay = LocalDate.now()
                .minusDays(TAG_GET_START_DAY);

        List<Tag> tags = tilTagService.getRecentWroteTags(startDay)
                .stream()
                .filter(tag -> tag.getTil()
                        .isNotDeleted())
                .toList();

        List<SubTag> subTags = tilTagService.getRecentSubTags(startDay)
                .stream()
                .filter(subTag -> subTag.getTag() != null && subTag.getTag()
                        .getTil() != null && subTag.getTag()
                        .getTil()
                        .isNotDeleted())
                .toList();

        Map<Tag, List<Tag>> tagRelationMap = tagRelationService.getRecentRelationTagMap();
        log.info(
                "crated tags size : {} subTags size : {} tagRelationMap size : {} ", tags.size(), subTags.size(),
                tagRelationMap.size()
        );

        return new TilTagRelations(tags, subTags, tagRelationMap);
    }
}
