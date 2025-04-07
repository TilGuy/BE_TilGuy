package com.tilguys.matilda.controller;

import com.tilguys.matilda.config.jwt.Jwt;
import com.tilguys.matilda.config.jwt.JwtTokenFactory;
import com.tilguys.matilda.security.GithubUserInfo;
import com.tilguys.matilda.security.service.GithubAuthService;
import com.tilguys.matilda.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/oauth"
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GithubAuthService githubAuthService;
    private final JwtTokenFactory jwtTokenFactory;

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie jwt = new Cookie(Jwt.getCookieName(), null);
        jwt.setMaxAge(0); // 쿠키의 expiration 타임을 0으로 하여 없앤다.
        jwt.setPath("/"); // 모든 경로에서 삭제 됬음을 알린다.
        return ResponseEntity.ok(ResponseEntity.accepted());
    }

    @PostMapping("/login")
    public ResponseEntity<?> getUserInfo(@RequestParam(value = "code") String code, HttpServletResponse response) {
        String accessToken = githubAuthService.getAccessToken(code);
        if (accessToken == null) {
            throw new OAuth2AuthenticationException("로그인에 실패하였습니다");
        }
        GithubUserInfo gitHubUserInfo = githubAuthService.getGitHubUserInfo(accessToken);
        String identifier = gitHubUserInfo.identifier();
        Cookie jwtCookie = jwtTokenFactory.createJwtCookieWithIdentifier(identifier);
        response.addCookie(jwtCookie);
        return ResponseEntity.ok(gitHubUserInfo);
    }
}
