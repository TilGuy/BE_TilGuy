package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.user.TilUser;

public record TilWithUserResponse(
        String title,
        String content,
        TagsResponse tags,
        String nickname,
        String avatarUrl
) {

    public TilWithUserResponse(Til til, TilUser tilUser) {
        this(
                til.getTitle(),
                til.getContent(),
                new TagsResponse(til.getTags()),
                tilUser.getNickname(),
                tilUser.getAvatarUrl()
        );
    }
}
