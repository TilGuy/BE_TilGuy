package com.tilguys.matilda.github.controller;

import com.tilguys.matilda.common.auth.SimpleUserInfo;
import com.tilguys.matilda.github.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubService gitHubService;

    @PutMapping("/storage")
    public ResponseEntity<Void> saveGitHubStorage(
            @RequestBody GitHubStorageRequest request,
            @AuthenticationPrincipal final SimpleUserInfo simpleUserInfo) {

        gitHubService.saveStorage(simpleUserInfo.id(), request);
        return ResponseEntity.ok().build();
    }
}
