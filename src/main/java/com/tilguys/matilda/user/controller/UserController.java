package com.tilguys.matilda.user.controller;

import com.tilguys.matilda.common.auth.GithubUserInfo;
import com.tilguys.matilda.common.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/profileUrl/{id}")
    public ResponseEntity<GithubUserInfo> getGithubUserInfo(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(authService.getGithubInfoById(id));
    }
}
