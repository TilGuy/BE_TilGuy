package com.tilguys.matilda.til.dto;

public record TilUpdateRequest(
        Long tilId,
        String content,
        boolean isPublic
) {
}
