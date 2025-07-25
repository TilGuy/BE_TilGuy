package com.tilguys.matilda.user.controller;

import com.tilguys.matilda.common.auth.GithubUserInfo;
import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.service.AuthService;
import com.tilguys.matilda.common.auth.service.GithubAuthService;
import com.tilguys.matilda.common.auth.service.UserRefreshTokenService;
import com.tilguys.matilda.common.auth.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/oauth")
@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final AuthService authService;
    private final GithubAuthService githubAuthService;
    private final Jwt jwt;
    private final UserRefreshTokenService userRefreshTokenService;
    private final UserService userService;

    @GetMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Long id) {
        Cookie jwtRemoveCookie = new Cookie(Jwt.getCookieName(), null);
        jwtRemoveCookie.setMaxAge(0);
        jwtRemoveCookie.setPath("/");
        userRefreshTokenService.deleteRefreshTokenByUserId(id);
        return ResponseEntity.ok(ResponseEntity.accepted().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam(value = "code") String code,
                                   HttpServletResponse response) {
        String accessToken = githubAuthService.getAccessToken(code);

        GithubUserInfo gitHubUserInfo = githubAuthService.getGitHubUserInfo(accessToken);
        authService.loginProcessByGithubInfo(gitHubUserInfo);
        userRefreshTokenService.addRefreshToken(gitHubUserInfo.identifier());

        Cookie jwtCookie = jwt.createJwtCookie();
        response.addCookie(jwtCookie);
        return ResponseEntity.ok(gitHubUserInfo);
    }
}
