package com.tilguys.matilda.til.dto;

import java.util.List;

public record TilDetailsResponse(
        List<TilDetailResponse> tils
) {
}
