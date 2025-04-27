package com.tilguys.matilda.home.dto;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.user.TilUser;
import java.util.List;

public record RecentTilResponse(
        String title,
        String content,
        List<String> tags,
        String nickname,
        String avatarUrl
) {

    public RecentTilResponse(Til til, TilUser tilUser) {
        this(
                til.getTitle(),
                til.getContent(),
                List.of("Java", "Spring"), // 태그 타입 생길 시 수정
                tilUser.getNickname(),
                tilUser.getAvatarUrl()
        );
    }
}
