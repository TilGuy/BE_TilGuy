package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDate;

public record TilCreateRequest(
        String title,
        String content,
        LocalDate date,
        boolean isPublic
) {

    public Til toEntity(final long userId) {
        return Til.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .date(date)
                .isPublic(isPublic)
                .build();
    }
}
