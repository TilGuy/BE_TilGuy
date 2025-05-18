package com.tilguys.matilda.til.dto;

import java.util.List;

public record TilWithUserResponses(
        List<TilWithUserResponse> recents
) {
}
