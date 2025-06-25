package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.user.TilUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TilDefinitionRequest(
        String title,
        @Size(min = 20, message = "내용은 최소 20자 이상이어야 합니다.")
        @NotBlank(message = "내용은 필수입니다.")
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
