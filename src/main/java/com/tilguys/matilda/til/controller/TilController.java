package com.tilguys.matilda.til.controller;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilCreateRequest;
import com.tilguys.matilda.til.dto.TilDatesResponse;
import com.tilguys.matilda.til.dto.TilDetailResponse;
import com.tilguys.matilda.til.dto.TilDetailsResponse;
import com.tilguys.matilda.til.service.TilService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/til")
@RestController
@RequiredArgsConstructor
public class TilController {

    private final TilService tilService;

    @PostMapping("")
    public ResponseEntity<?> saveTil(@RequestBody final TilCreateRequest createRequest) {
        Til saved = tilService.createTil(createRequest);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/dates")
    public ResponseEntity<?> getAllTilDates(@AuthenticationPrincipal final Long userId) {
        TilDatesResponse datesForUser = tilService.getAllTilDatesByUserId(userId);
        return ResponseEntity.ok(datesForUser);
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentTilById(@AuthenticationPrincipal final Long userId) {
        List<TilDetailResponse> recentTils = tilService.getRecentTilById(userId);
        return ResponseEntity.ok(recentTils);
    }

    @GetMapping("/main")
    public ResponseEntity<?> getMainTil(@RequestParam(defaultValue = "0") final int page,
                                        @RequestParam(defaultValue = "10") final int size) {
        Page<TilDetailResponse> tilPage = tilService.getTilByPagination(page, size);
        return ResponseEntity.ok(tilPage);
    }

    @GetMapping("/range")
    public ResponseEntity<?> getTilByDateRange(@AuthenticationPrincipal final Long userId,
                                               @RequestParam final LocalDate from,
                                               @RequestParam final LocalDate to) {
        TilDetailsResponse tilsInRange = tilService.getTilByDateRange(userId, from, to);
        return ResponseEntity.ok(tilsInRange);
    }
}
