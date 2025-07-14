package com.tilguys.matilda.github.controller;

import com.tilguys.matilda.common.auth.SimpleUserInfo;
import com.tilguys.matilda.github.domain.GitHubStorage;
import com.tilguys.matilda.github.dto.GitHubStorageResponse;
import com.tilguys.matilda.github.service.GitHubStorageService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github/storage")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubStorageService gitHubStorageService;

    @GetMapping
    public ResponseEntity<GitHubStorageResponse> getGitHubStorage(
            @AuthenticationPrincipal final SimpleUserInfo simpleUserInfo
    ) {
        Optional<GitHubStorage> gitHubStorage = gitHubStorageService.getGitHubStorage(simpleUserInfo.id());
        return gitHubStorage.map(hubStorage -> ResponseEntity.ok(new GitHubStorageResponse(hubStorage)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping
    public ResponseEntity<Void> saveGitHubStorage(
            @RequestBody GitHubStorageRequest request,
            @AuthenticationPrincipal final SimpleUserInfo simpleUserInfo) {

        gitHubStorageService.saveOrUpdateSettings(simpleUserInfo.id(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/active")
    public ResponseEntity<Void> updateGitHubStorageActive(
            @AuthenticationPrincipal final SimpleUserInfo simpleUserInfo
    ) {
        gitHubStorageService.updateGitHubStorageActive(simpleUserInfo.id());
        return ResponseEntity.ok().build();
    }
}
