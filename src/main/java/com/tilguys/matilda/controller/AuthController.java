package com.tilguys.matilda.controller;

import com.tilguys.matilda.config.jwt.JwtTokenFactory;
import com.tilguys.matilda.security.GithubUserInfo;
import com.tilguys.matilda.security.service.GithubAuthService;
import com.tilguys.matilda.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/oauth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GithubAuthService githubAuthService;
    private final JwtTokenFactory jwtTokenFactory;

    @GetMapping("/hi")
    public ResponseEntity<?> hi() {
        System.out.println("hi");
        return ResponseEntity.ok("hi");
    }

    @PostMapping("/login")
    public ResponseEntity<?> getUserInfo(@RequestParam(value = "code") String code, HttpServletResponse response) {
        System.out.println(code);
        String accessToken = githubAuthService.getAccessToken(code);
        if (accessToken == null) {
            throw new OAuth2AuthenticationException("로그인에 실패하였습니다");
        }
        System.out.println(accessToken);
        GithubUserInfo gitHubUserInfo = githubAuthService.getGitHubUserInfo(accessToken);

        Authentication authentication = authService.createAuthenticationFromName(gitHubUserInfo.identifier());
        Cookie jwtCookie = jwtTokenFactory.createJwtCookie(response, authentication);
        response.addCookie(jwtCookie);
        return ResponseEntity.ok(gitHubUserInfo);
    }
}
