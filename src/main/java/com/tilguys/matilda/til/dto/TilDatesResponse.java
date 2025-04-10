package com.tilguys.matilda.til.dto;

import java.time.LocalDate;
import java.util.List;

public record TilDatesResponse(
        List<LocalDate> dates
) {
}
