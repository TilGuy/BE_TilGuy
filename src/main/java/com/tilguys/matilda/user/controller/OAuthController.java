package com.tilguys.matilda.user.controller;

import com.tilguys.matilda.common.auth.GithubUserInfo;
import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.service.AuthService;
import com.tilguys.matilda.common.auth.service.GithubAuthService;
import com.tilguys.matilda.common.auth.service.UserRefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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

    @GetMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Long id) {
        Cookie jwt = new Cookie(Jwt.getCookieName(), null);
        jwt.setMaxAge(0); // 쿠키의 expiration 타임을 0으로 하여 없앤다.
        jwt.setPath("/"); // 모든 경로에서 삭제 됬음을 알린다.
        userRefreshTokenService.deleteRefreshTokenByUserId(id);
        return ResponseEntity.ok(ResponseEntity.accepted().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam(value = "code") String code,
                                   HttpServletResponse response) {
        String accessToken = githubAuthService.getAccessToken(code);
        if (accessToken == null) {
            throw new OAuth2AuthenticationException("로그인에 실패하였습니다");
        }

        GithubUserInfo gitHubUserInfo = githubAuthService.getGitHubUserInfo(accessToken);
        authService.loginProcessByGithubInfo(gitHubUserInfo);
        userRefreshTokenService.addRefreshToken(gitHubUserInfo.identifier());

        Cookie jwtCookie = jwt.createJwtCookie();
        response.addCookie(jwtCookie);
        return ResponseEntity.ok(gitHubUserInfo);
    }
}
