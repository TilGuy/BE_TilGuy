package com.tilguys.matilda.til.dto;

import java.time.LocalDate;

public record TilUpdateRequest(
        String content,
        LocalDate date,
        boolean isPublic,
        String title
) {

}
