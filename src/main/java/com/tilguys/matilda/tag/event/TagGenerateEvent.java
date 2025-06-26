package com.tilguys.matilda.tag.event;

import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.domain.Til;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TagGenerateEvent {

    private final TilTagService tilTagService;

    public TagGenerateEvent(TilTagService tilTagService) {
        this.tilTagService = tilTagService;
    }

    @EventListener
    public void tagGenerate(Til til) {
    }
}
