package com.tilguys.matilda.common;

import com.tilguys.matilda.tag.schedule.TagScheduledJob;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ServerInitiate {

    private final TagScheduledJob tagScheduledJob;

    public ServerInitiate(TagScheduledJob tagScheduledJob) {
        this.tagScheduledJob = tagScheduledJob;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        tagScheduledJob.updateRecentTagRelations();
    }
}
