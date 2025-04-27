package com.tilguys.matilda.home.controller;

import com.tilguys.matilda.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentTils() {
        return ResponseEntity.ok(homeService.getRecentTils());
    }
}
