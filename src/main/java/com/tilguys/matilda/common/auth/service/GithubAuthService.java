package com.tilguys.matilda.common.auth.service;

import com.tilguys.matilda.common.auth.GithubUserInfo;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GithubAuthService {

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    @Value("${github.user-info-uri}")
    private String userInfoUrl;

    public String getAccessToken(String code) {
        String tokenUrl = "https://github.com/login/oauth/access_token";
        String requestBody = oauthRequestBody(code);
        HttpHeaders headers = oauthHttpHeader();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity,
                Map.class);
        Map<String, Object> map = response.getBody();
        return extractAccessToken(map);
    }

    private static HttpHeaders oauthHttpHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private String oauthRequestBody(String code) {
        return "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&code=" + code;
    }

    private String extractAccessToken(Map<String, Object> responseBody) {
        return (String) responseBody.get("access_token");
    }

    public GithubUserInfo getGitHubUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> body = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, Map.class).getBody();

        String identifier = body.get("login");
        String avatarUrl = body.get("avatar_url");

        return new GithubUserInfo(identifier, avatarUrl);
    }
}


