package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.user.TilUser;
import java.time.LocalDate;

public record TilCreateRequest(
        String title,
        String content,
        LocalDate date,
        boolean isPublic
) {

    public Til toEntity(TilUser user) {
        return Til.builder()
                .tilUser(user)
                .title(title)
                .content(content)
                .date(date)
                .isPublic(isPublic)
                .build();
    }
}
