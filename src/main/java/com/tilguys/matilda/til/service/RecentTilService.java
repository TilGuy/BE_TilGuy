package com.tilguys.matilda.til.service;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilWithUserResponse;
import com.tilguys.matilda.til.dto.TilWithUserResponses;
import com.tilguys.matilda.til.repository.TilRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecentTilService {

    private final TilRepository tilRepository;

    public TilWithUserResponses getRecentTils(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Til> recentTils = tilRepository.findAllByIsPublicTrueAndIsDeletedFalse(pageRequest);
        List<TilWithUserResponse> responses = convertToRecentTilResponses(recentTils.getContent());
        return new TilWithUserResponses(responses);
    }

    private List<TilWithUserResponse> convertToRecentTilResponses(List<Til> recentTils) {
        return recentTils.stream()
                .map(this::createRecentTilResponse)
                .toList();
    }

    private TilWithUserResponse createRecentTilResponse(Til til) {
        return new TilWithUserResponse(til);
    }
}
