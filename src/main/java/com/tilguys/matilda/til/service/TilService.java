package com.tilguys.matilda.til.service;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilCreateRequest;
import com.tilguys.matilda.til.dto.TilDatesResponse;
import com.tilguys.matilda.til.dto.TilDetailResponse;
import com.tilguys.matilda.til.dto.TilDetailsResponse;
import com.tilguys.matilda.til.dto.TilUpdateRequest;
import com.tilguys.matilda.til.repository.TilRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TilService {

    private static final int RECENT_TIL_SIZE = 4;

    private final TilRepository tilRepository;

    public Til createTil(final TilCreateRequest createRequest) {
        Til newTil = createRequest.toEntity();
        return tilRepository.save(newTil);
    }

    public Page<TilDetailResponse> getRecentTilById(final Long userId) {
        return getUserTilByPagination(0, RECENT_TIL_SIZE, userId);
    }

    public Page<TilDetailResponse> getTilByPagination(final int page, final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Til> tilPage = tilRepository.findAll(pageable);

        return tilPage.map(TilDetailResponse::fromEntity);
    }

    public Page<TilDetailResponse> getUserTilByPagination(final int page, final int size, final Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Til> tilPage = tilRepository.findAllByUserId(pageable, userId);

        return tilPage.map(TilDetailResponse::fromEntity);
    }

    public TilDatesResponse getAllTilDatesByUserId(final Long userId) {
        List<LocalDate> all = tilRepository.findByUserId(userId)
                .stream()
                .map(til -> til.getCreatedAt().toLocalDate())
                .toList();

        return new TilDatesResponse(all);
    }

    public void updateTil(final TilUpdateRequest updateRequest) {
        Til til = getTilByTilId(updateRequest.tilId());
        til.updateContentAndVisibility(updateRequest.content(), updateRequest.isPublic());
    }

    public void deleteTil(final Long tilId) {
        if (!tilRepository.existsById(tilId)) {
            throw new IllegalArgumentException();
        }
        Til til = getTilByTilId(tilId);
        til.markAsDeleted();
    }

    public TilDetailsResponse getTilByDateRange(final Long userId, final LocalDate from, final LocalDate to) {
        List<Til> tils = tilRepository.findByUserId(userId);

        List<TilDetailResponse> responseList = tils.stream()
                .filter(til -> til.isWithinDateRange(from, to))
                .map(TilDetailResponse::fromEntity)
                .toList();

        return new TilDetailsResponse(responseList);
    }

    private Til getTilByTilId(final Long tilId) {
        return tilRepository.findById(tilId)
                .orElseThrow(IllegalArgumentException::new);
    }
}
