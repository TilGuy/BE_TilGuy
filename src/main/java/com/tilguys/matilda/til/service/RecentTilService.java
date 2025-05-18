package com.tilguys.matilda.til.service;

import com.tilguys.matilda.common.auth.exception.NotExistUserException;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.RecentTilResponse;
import com.tilguys.matilda.til.dto.RecentTilResponses;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecentTilService {

    private final UserRepository userRepository;
    private final TilRepository tilRepository;

    public RecentTilResponses getRecentTils() {
        List<Til> recentTils = tilRepository.findTop10ByIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc();
        List<RecentTilResponse> responses = convertToRecentTilResponses(recentTils);
        return new RecentTilResponses(responses);
    }

    private List<RecentTilResponse> convertToRecentTilResponses(List<Til> recentTils) {
        return recentTils.stream()
                .map(this::createRecentTilResponse)
                .toList();
    }

    private RecentTilResponse createRecentTilResponse(Til til) {
        TilUser user = userRepository.findById(til.getUserId())
                .orElseThrow(NotExistUserException::new);
        return new RecentTilResponse(til, user);
    }
}
