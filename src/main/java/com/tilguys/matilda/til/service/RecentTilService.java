package com.tilguys.matilda.til.service;

import com.tilguys.matilda.common.auth.exception.NotExistUserException;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilWithUserResponse;
import com.tilguys.matilda.til.dto.TilWithUserResponses;
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

    public TilWithUserResponses getRecentTils() {
        List<Til> recentTils = tilRepository.findTop10ByIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc();
        List<TilWithUserResponse> responses = convertToRecentTilResponses(recentTils);
        return new TilWithUserResponses(responses);
    }

    private List<TilWithUserResponse> convertToRecentTilResponses(List<Til> recentTils) {
        return recentTils.stream()
                .map(this::createRecentTilResponse)
                .toList();
    }

    private TilWithUserResponse createRecentTilResponse(Til til) {
        TilUser user = userRepository.findById(til.getUserId())
                .orElseThrow(NotExistUserException::new);
        return new TilWithUserResponse(til, user);
    }
}
