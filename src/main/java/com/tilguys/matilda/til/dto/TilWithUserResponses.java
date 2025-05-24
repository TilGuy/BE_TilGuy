package com.tilguys.matilda.til.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;

public record TilWithUserResponses(
        @JsonValue
        List<TilWithUserResponse> tilWithUsers
) {
}
