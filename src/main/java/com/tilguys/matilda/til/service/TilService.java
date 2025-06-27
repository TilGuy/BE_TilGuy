package com.tilguys.matilda.til.service;

import com.tilguys.matilda.reference.service.TilReferenceService;
import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.PagedTilResponse;
import com.tilguys.matilda.til.dto.TilDatesResponse;
import com.tilguys.matilda.til.dto.TilDefinitionRequest;
import com.tilguys.matilda.til.dto.TilDetailResponse;
import com.tilguys.matilda.til.dto.TilDetailsResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public Til createTil(final TilDefinitionRequest tilCreateDto, final long userId) {
        //// 2. TIL 생성 서비스 (이벤트 발행)
        //@Service
        //public class TilService {
        //
        //    private final ApplicationEventPublisher eventPublisher;
        //    private final TilRepository tilRepository;
        //
        //    public TilService(ApplicationEventPublisher eventPublisher, TilRepository tilRepository) {
        //        this.eventPublisher = eventPublisher;
        //        this.tilRepository = tilRepository;
        //    }
        //
        //    public Til createTil(String content, Long userId) {
        //        // TIL 저장
        //        Til til = new Til(content, userId);
        //        Til savedTil = tilRepository.save(til);
        //
        //        // 이벤트 발행
        //        TilCreatedEvent event = new TilCreatedEvent(
        //            savedTil.getId(),
        //            savedTil.getContent(),
        //            savedTil.getUserId()
        //        );
        //        eventPublisher.publishEvent(event);
        //
        //        return savedTil;
        //    }
        //}
        boolean exists = tilRepository.existsByDateAndTilUserIdAndIsDeletedFalse(tilCreateDto.date(), userId);
        if (exists) {
            throw new IllegalArgumentException("같은 날에 작성된 게시물이 존재합니다!");
        }

        TilUser user = userService.findById(userId);
        Til newTil = tilCreateDto.toEntity(user);
        //이벤트 발행 필요
        return tilRepository.save(newTil);
    }

    public TilDatesResponse getAllTilDatesByUserId(final Long userId) {
        List<LocalDate> all = tilRepository.findByTilUserId(userId).stream()
                .filter(Til::isNotDeleted)
                .map(Til::getDate)
                .toList();

        return new TilDatesResponse(all);
    }

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
    }

    public void deleteTil(final Long tilId, final Long userId) {
        if (!tilRepository.existsById(tilId)) {
            throw new IllegalArgumentException();
        }
        Til til = getTilByTilId(tilId);
        til.markAsDeletedBy(userId);
    }

    public TilDetailsResponse getTilByDateRange(final Long userId, final LocalDate from, final LocalDate to) {
        List<Til> tils = tilRepository.findAllByTilUserIdAndDateBetweenAndIsDeleted(userId, from, to, false);

        List<TilDetailResponse> responseList = tils.stream()
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

    private void validateDeleted(Til til) {
        if (til.isDeleted()) {
            throw new IllegalArgumentException("삭제된 TIL은 수정할 수 없습니다.");
        }
    }
}
