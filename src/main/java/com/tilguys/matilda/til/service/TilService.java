package com.tilguys.matilda.til.service;

import com.tilguys.matilda.github.service.GitHubWorkflowService;
import com.tilguys.matilda.reference.event.ReferenceCreateEvent;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilDatesResponse;
import com.tilguys.matilda.til.dto.TilDefinitionRequest;
import com.tilguys.matilda.til.dto.TilDetailResponse;
import com.tilguys.matilda.til.dto.TilDetailsResponse;
import com.tilguys.matilda.til.dto.TilReadAllResponse;
import com.tilguys.matilda.til.event.TilCreatedEvent;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.service.TilUserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TilService {

    private final TilRepository tilRepository;
    private final TilUserService userService;
    private final GitHubWorkflowService gitHubWorkflowService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Til createTil(final TilDefinitionRequest tilCreateDto, final long userId) {
        boolean exists = tilRepository.existsByDateAndTilUserIdAndIsDeletedFalse(tilCreateDto.date(), userId);
        if (exists) {
            throw new IllegalArgumentException("같은 날에 작성된 게시물이 존재합니다!");
        }

        TilUser user = userService.findById(userId);
        Til newTil = tilCreateDto.toEntity(user);
        Til til = tilRepository.save(newTil);

        eventPublisher.publishEvent(
                new TilCreatedEvent(til.getTilId(), til.getContent(), user.getId())
        );

        eventPublisher.publishEvent(
                new ReferenceCreateEvent(til.getTilId(), til.getContent())
        );

        gitHubWorkflowService.uploadTilToGitHub(til);
        return til;
    }

    @Transactional(readOnly = true)
    public TilDatesResponse getAllTilDatesByUserId(final Long userId) {
        List<LocalDate> all = tilRepository.findByTilUserId(userId).stream()
                .filter(Til::isNotDeleted)
                .map(Til::getDate)
                .toList();

        return new TilDatesResponse(all);
    }

    @Transactional
    public void updateTil(final Long tilId, final TilDefinitionRequest tilUpdateDto, final long userId) {
        Til til = getTilByTilId(tilId);
        validateDeleted(til);
        LocalDate targetDate = tilUpdateDto.date();
        boolean exists = tilRepository.existsByDateAndTilUserIdAndIsDeletedFalse(targetDate, userId);
        if (exists && !targetDate.equals(til.getDate())) {
            throw new IllegalArgumentException("해당 날짜에 이미 작성된 게시물이 존재합니다!");
        }
        til.update(
                tilUpdateDto.content(),
                tilUpdateDto.isPublic(),
                targetDate,
                tilUpdateDto.title()
        );

        gitHubWorkflowService.updateTilToGitHub(til);
    }

    @Transactional
    public void deleteTil(final Long tilId, final Long userId) {
        if (!tilRepository.existsById(tilId)) {
            throw new IllegalArgumentException();
        }
        Til til = getTilByTilId(tilId);
        til.markAsDeletedBy(userId);

        gitHubWorkflowService.deleteTilToGitHub(til);
    }

    @Transactional(readOnly = true)
    public List<TilReadAllResponse> getPublicTils(LocalDateTime cursorDate, Long cursorId, int size) {
        List<Til> tils = tilRepository.findPublicTilsWithAllInfo(
                cursorDate, cursorId, Pageable.ofSize(size)
        );

        return tils.stream()
                .map(TilReadAllResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public TilDetailsResponse getTilByDateRange(final Long userId, final LocalDate from, final LocalDate to) {
        List<Til> tils = tilRepository.findAllByTilUserIdAndDateBetweenAndIsDeleted(userId, from, to, false);

        List<TilDetailResponse> responseList = tils.stream()
                .map(TilDetailResponse::fromEntity)
                .toList();

        return new TilDetailsResponse(responseList);
    }

    @Transactional(readOnly = true)
    public Til getTilByTilId(final Long id) {
        return tilRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public Til getTilByTilIdAndIsDeletedFalse(final Long tilId) {
        return tilRepository.findByTilIdAndIsDeletedFalse(tilId)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
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

    private void validateDeleted(Til til) {
        if (til.isDeleted()) {
            throw new IllegalArgumentException("삭제된 TIL은 수정할 수 없습니다.");
        }
    }
}
