package com.tilguys.matilda.til.service;

import com.tilguys.matilda.reference.service.TilReferenceService;
import com.tilguys.matilda.tag.domain.TilTags;
import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.domain.Reference;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.PagedTilResponse;
import com.tilguys.matilda.til.dto.TilCreateRequest;
import com.tilguys.matilda.til.dto.TilDatesResponse;
import com.tilguys.matilda.til.dto.TilDetailResponse;
import com.tilguys.matilda.til.dto.TilDetailsResponse;
import com.tilguys.matilda.til.dto.TilUpdateRequest;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.service.TilUserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TilService {

    private final TilRepository tilRepository;
    private final TilTagService tilTagService;
    private final TilReferenceService tilReferenceService;
    private final TilUserService userService;

    public PagedTilResponse getPublicTils(int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "date");
        Page<Til> tilPage = tilRepository.findAllByIsPublicTrueAndIsDeletedFalse(pageRequest);
        return new PagedTilResponse(tilPage);
    }

    @Transactional
    public Til createTil(final TilCreateRequest tilCreateDto, final long userId) {
        boolean exists = tilRepository.existsByDateAndTilUserIdAndIsDeletedFalse(tilCreateDto.date(), userId);
        if (exists) {
            throw new IllegalArgumentException("같은 날에 작성된 게시물이 존재합니다!");
        }

        TilUser user = userService.findById(userId);
        Til newTil = tilCreateDto.toEntity(user);
        Til til = tilRepository.save(newTil);

        String tilResponseJson = tilTagService.requestTilTagResponseJson(til.getContent());

        List<Tag> tags = tilTagService.extractTilTags(tilResponseJson)
                .stream()
                .toList();

        String tagResults = tags.stream()
                .map(Tag::getTagString)
                .collect(Collectors.joining(","));
        log.debug("{}=> {} => 추출된 태그 =>{}", til.getContent(), tilResponseJson, tagResults);

        til.updateTags(tags);
        List<Reference> references = tilReferenceService.extractTilReference(til.getContent());
        til.updateReferences(references);

        TilTags tilTags = new TilTags(tags);
        tilTagService.createSubTags(tilResponseJson, tilTags);
        return til;
    }

    public Page<TilDetailResponse> getTilByPagination(final int page, final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Til> tilPage = tilRepository.findAll(pageable);

        return tilPage.map(TilDetailResponse::fromEntity);
    }

    public TilDatesResponse getAllTilDatesByUserId(final Long userId) {
        List<LocalDate> all = tilRepository.findByTilUserId(userId).stream()
                .filter(Til::isNotDeleted)
                .map(Til::getDate)
                .toList();

        return new TilDatesResponse(all);
    }

    public void updateTil(final Long tilId, final TilUpdateRequest tilUpdateDto) {
        Til til = getTilByTilId(tilId);
        til.update(
                tilUpdateDto.content(),
                tilUpdateDto.isPublic(),
                tilUpdateDto.date(),
                tilUpdateDto.title()
        );
    }

    public void deleteTil(final Long tilId) {
        if (!tilRepository.existsById(tilId)) {
            throw new IllegalArgumentException();
        }
        Til til = getTilByTilId(tilId);
        til.markAsDeleted();
    }

    public TilDetailsResponse getTilByDateRange(final Long userId, final LocalDate from, final LocalDate to) {
        List<Til> tils = tilRepository.findByTilUserId(userId);

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

    public List<Til> getTilsByIds(List<Long> tilIds) {
        List<Til> tils = new ArrayList<>();
        for (Long tilId : tilIds) {
            tils.add(getTilByTilId(tilId));
        }
        return Collections.unmodifiableList(tils);
    }

    @Transactional(readOnly = true)
    public List<Til> getRecentWroteTil(LocalDateTime startTime) {
        return tilRepository.findByCreatedAtGreaterThanEqual(startTime);
    }
}
