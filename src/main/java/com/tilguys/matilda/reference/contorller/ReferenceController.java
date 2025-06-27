package com.tilguys.matilda.reference.contorller;

import com.tilguys.matilda.reference.service.TilReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/til/reference")
@RestController
@RequiredArgsConstructor
public class ReferenceController {

    private final TilReferenceService tilReferenceService;

    @GetMapping
    public ResponseEntity<?> getReference(@RequestParam Long tilId) {
        return ResponseEntity.ok(tilReferenceService.getReferencesByTilId(tilId));
    }
}
