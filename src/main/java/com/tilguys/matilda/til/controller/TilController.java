package com.tilguys.matilda.til.controller;

import com.tilguys.matilda.common.auth.SimpleUserInfo;
import com.tilguys.matilda.slack.service.SlackService;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilDatesResponse;
import com.tilguys.matilda.til.dto.TilDefinitionRequest;
import com.tilguys.matilda.til.dto.TilDetailResponse;
import com.tilguys.matilda.til.dto.TilDetailsResponse;
import com.tilguys.matilda.til.dto.TilWithUserResponse;
import com.tilguys.matilda.til.service.RecentTilService;
import com.tilguys.matilda.til.service.TilService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/til")
@RestController
@RequiredArgsConstructor
public class TilController {

    private final TilService tilService;
    private final RecentTilService recentTilService;
    private final SlackService slackService;

    @GetMapping("/all")
    public ResponseEntity<?> getPublicTils(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdDate,
            @RequestParam(required = false) Long tilId,
            @RequestParam(defaultValue = "10")
            @Max(value = 100)
            int size
    ) {
        return ResponseEntity.ok(tilService.getPublicTils(
                createdDate, tilId, size)
        );
    }

    @PostMapping
    public ResponseEntity<?> saveTil(@Valid @RequestBody final TilDefinitionRequest createRequest,
                                     @AuthenticationPrincipal final SimpleUserInfo simpleUserInfo) {
        Til til = tilService.createTil(createRequest, simpleUserInfo.id());

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yy-MM월-dd일");
        String dateString = dateTimeFormatter.format(til.getDate());
        slackService.sendTilWriteAlarm(til.getContent(), simpleUserInfo.nickname(), dateString, til.getTags());

        return ResponseEntity.ok(TilDetailResponse.fromEntity(til));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteTil(
            @PathVariable final Long id,
            @AuthenticationPrincipal final SimpleUserInfo simpleUserInfo
    ) {
        tilService.deleteTil(id, simpleUserInfo.id());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTil(
            @PathVariable final Long id,
            @Valid @RequestBody final TilDefinitionRequest request,
            @AuthenticationPrincipal final SimpleUserInfo simpleUserInfo
    ) {
        tilService.updateTil(id, request, simpleUserInfo.id());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dates")
    public ResponseEntity<?> getAllTilDates(@AuthenticationPrincipal final SimpleUserInfo simpleUserInfo) {
        TilDatesResponse datesForUser = tilService.getAllTilDatesByUserId(simpleUserInfo.id());
        return ResponseEntity.ok(datesForUser);
    }

    @GetMapping("/range")
    public ResponseEntity<?> getTilByDateRange(@AuthenticationPrincipal final SimpleUserInfo simpleUserInfo,
                                               @RequestParam final LocalDate from,
                                               @RequestParam final LocalDate to) {
        TilDetailsResponse tilsInRange = tilService.getTilByDateRange(simpleUserInfo.id(), from, to);
        return ResponseEntity.ok(tilsInRange);
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentTils(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(recentTilService.getRecentTils(page, size));
    }

    @GetMapping("/{tilId}")
    public ResponseEntity<?> getTilById(@PathVariable final Long tilId) {
        Til til = tilService.getTilByTilId(tilId);
        return ResponseEntity.ok(new TilWithUserResponse(til));
    }
}
