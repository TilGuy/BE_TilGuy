package com.tilguys.matilda.github.client;

import com.tilguys.matilda.github.domain.GitHubUpload;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GitHubClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RestTemplate restTemplate;

    public void uploadTilContent(GitHubUpload gitHubUpload) {
        String encodedContent = encodeContent(gitHubUpload.getContents());
        HttpEntity<Map<String, String>> requestEntity = createRequestEntity(gitHubUpload, encodedContent);
        restTemplate.put(gitHubUpload.getUploadsUrl(), requestEntity);
    }

    private String encodeContent(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    private HttpEntity<Map<String, String>> createRequestEntity(GitHubUpload gitHubUpload, String encodedContent) {
        return new HttpEntity<>(
                createRequestBody(gitHubUpload.getCommitMessage(), encodedContent),
                createHeaders(gitHubUpload.getAccessToken())
        );
    }

    private Map<String, String> createRequestBody(String commitMessage, String encodedContent) {
        return Map.of(
                "message", commitMessage,
                "content", encodedContent
        );
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
        return headers;
    }
}
