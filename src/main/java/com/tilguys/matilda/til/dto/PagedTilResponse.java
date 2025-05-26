package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Til;
import java.util.List;
import org.springframework.data.domain.Page;

public record PagedTilResponse(
        List<TilWithUserResponse> tils,
        boolean hasNext,
        int currentPage
) {
        
        public PagedTilResponse(Page<Til> tilPage) {
                this(
                       convertToSummaryResponses(tilPage),
                       tilPage.hasNext(),
                       tilPage.getNumber() 
                );
        }
        
        private static List<TilWithUserResponse> convertToSummaryResponses(Page<Til> tilPage) {
                return tilPage.stream()
                        .map(TilWithUserResponse::new)
                        .toList();
        } 
}
