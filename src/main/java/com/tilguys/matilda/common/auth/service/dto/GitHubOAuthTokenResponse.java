package com.tilguys.matilda.common.auth.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * GitHub OAuth Access Token 응답을 위한 DTO 클래스
 * <p>
 * GitHub 공식 문서: https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/authorizing-oauth-apps
 * <p>
 * 예시 응답: { "access_token": "gho_16C7e42F292c6912E7710c838347Ae178B4a", "scope": "repo,gist", "token_type": "bearer" }
 */
@RequiredArgsConstructor
@Getter
@Setter
public class GitHubOAuthTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("token_type")
    private String tokenType;
}
