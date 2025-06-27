package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDateTime;

public record TilReadAllResponse(
        Long id,
        String title,
        String content,
        TagsResponse tags,
        String nickname,
        String avatarUrl,
        LocalDateTime createdAt
) {

    public TilReadAllResponse(Til til) {
        this(
                til.getTilId(),
                til.getTitle(),
                til.getContent(),
                new TagsResponse(til.getTags()),
                til.getTilUser().getNickname(),
                til.getTilUser().getAvatarUrl(),
                til.getCreatedAt()
        );
    }
}
