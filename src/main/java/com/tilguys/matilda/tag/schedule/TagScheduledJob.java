package com.tilguys.matilda.tag.schedule;


import com.tilguys.matilda.tag.service.TagRelationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TagScheduledJob {

    private final TagRelationService tagRelationService;

    public TagScheduledJob(TagRelationService tagRelationService) {
        this.tagRelationService = tagRelationService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateTagRelations() {
        tagRelationService.updateCoreTagsRelation();
    }
}
