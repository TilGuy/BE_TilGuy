package com.tilguys.matilda.til.event;

public class TilCreatedEvent {
    private final Long tilId;
    private final String tilContent;
    private final Long userId;

    public TilCreatedEvent(Long tilId, String tilContent, Long userId) {
        this.tilId = tilId;
        this.tilContent = tilContent;
        this.userId = userId;
    }

    // getters
    public Long getTilId() {
        return tilId;
    }

    public String getTilContent() {
        return tilContent;
    }

    public Long getUserId() {
        return userId;
    }
}
