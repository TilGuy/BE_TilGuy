package com.tilguys.matilda.til.service;

import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilCreateRequest;
import com.tilguys.matilda.til.dto.TilDatesResponse;
import com.tilguys.matilda.til.dto.TilDetailResponse;
import com.tilguys.matilda.til.dto.TilDetailsResponse;
import com.tilguys.matilda.til.dto.TilUpdateRequest;
import com.tilguys.matilda.til.repository.TilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TilService {

    private static final int RECENT_TIL_SIZE = 4;

    private final TilRepository tilRepository;
    private final TilTagService tilTagService;

    @Transactional
    public Til createTil(final TilCreateRequest tilCreateDto, final long userId) {
        boolean exists = tilRepository.existsByDateAndUserIdAndIsDeletedFalse(tilCreateDto.date(), userId);
        if (exists) {
            throw new IllegalArgumentException("같은 날에 작성된 게시물이 존재합니다!");
        }
        Til newTil = tilCreateDto.toEntity(userId);
        Til til = tilRepository.save(newTil);
        List<Tag> tags = tilTagService.extractTilTags(til.getContent())
                .stream()
                .toList();
        til.updateTags(tags);
        return til;
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
        List<LocalDate> all = tilRepository.findByUserId(userId).stream()
                .filter(Til::isNotDeleted)
                .map(Til::getDate)
                .toList();

        return new TilDatesResponse(all);
    }

    public void updateTil(final TilUpdateRequest tilUpdateDto) {
        Til til = getTilByTilId(tilUpdateDto.tilId());
        til.updateContentAndVisibility(tilUpdateDto.content(), tilUpdateDto.isPublic());
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
                .filter(Til::isNotDeleted)
                .map(TilDetailResponse::fromEntity)
                .toList();

        return new TilDetailsResponse(responseList);
    }

    public Til getTilByTilId(final Long tilId) {
        return tilRepository.findById(tilId)
                .orElseThrow(IllegalArgumentException::new);
    }
}
