package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Til;

public record TilCreateRequest(
        String title,
        String content,
        boolean isPublic
) {

    public Til toEntity() {
        return Til.builder()
                .title(title)
                .content(content)
                .isPublic(isPublic)
                .build();
    }
}
