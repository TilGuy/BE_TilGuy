package com.tilguys.matilda.til.service;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilCreateRequest;
import com.tilguys.matilda.til.dto.TilUpdateRequest;
import com.tilguys.matilda.til.repository.TilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TilService {

    private final TilRepository tilRepository;

    public Til createTil(final TilCreateRequest createRequest) {
        Til newTil = createRequest.toEntity();
        return tilRepository.save(newTil);
    }

    public Til getTilByTilId(final Long tilId) {
        return tilRepository.findById(tilId)
                .orElseThrow(IllegalArgumentException::new);
    }

    public void updateTil(final TilUpdateRequest updateRequest) {
        Til til = getTilByTilId(updateRequest.tilId());
        til.update(updateRequest.content(), updateRequest.isPublic());
    }

    public void deleteTil(Long tilId) {
        if (!tilRepository.existsById(tilId)) {
            throw new IllegalArgumentException();
        }
        Til til = getTilByTilId(tilId);
        til.markAsDeleted();
    }
}
