package com.tilguys.matilda.til.service;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilWithUserResponse;
import com.tilguys.matilda.til.repository.TilRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecentTilService {

    private final TilRepository tilRepository;

    @Transactional(readOnly = true)
    public List<TilWithUserResponse> getRecentTils() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt");
        Page<Til> recentTils = tilRepository.findAllByIsPublicTrueAndIsDeletedFalse(pageRequest);
        return convertToRecentTilResponses(recentTils.getContent());
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
