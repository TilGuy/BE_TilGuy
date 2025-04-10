package com.tilguys.matilda.til.controller;

import com.tilguys.matilda.til.service.TilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/til")
@RestController
@RequiredArgsConstructor
public class TilController {

    private final TilService tilService;

    @GetMapping("/today")
    public ResponseEntity<?> getTodayTil() {
        return ResponseEntity.ok(tilService.getTodayTil(1L)); // todo : 유저 정보 반환으로 변경
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentTilById() {
        return ResponseEntity.ok(tilService.getRecentTilById(1L)); // todo : 유저 정보 반환으로 변경
    }

    @GetMapping("/main")
    public ResponseEntity<?> getMainTil(@RequestParam(defaultValue = "0") final int page,
                                        @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(tilService.getMainTilByPagination(page, size));
    }
}
