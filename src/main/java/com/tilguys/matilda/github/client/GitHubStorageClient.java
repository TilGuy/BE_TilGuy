package com.tilguys.matilda.github.client;

import com.tilguys.matilda.github.domain.GitHubGetPayload;
import com.tilguys.matilda.github.domain.GitHubUploadPayload;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GitHubStorageClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RestTemplate restTemplate;

    public void uploadTilContent(GitHubUploadPayload uploadPayload) {
        String encodedContent = encodeContent(uploadPayload.getContents());
        HttpEntity<Map<String, String>> requestEntity = createRequestEntity(uploadPayload, encodedContent);
        restTemplate.put(uploadPayload.getUploadsUrl(), requestEntity);
    }

    public void getRepository(GitHubGetPayload getPayload) {
        HttpHeaders headers = createAuthorizationHeaders(getPayload.getAccessToken());
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        restTemplate.exchange(
                getPayload.getGetUrl(),
                HttpMethod.GET,
                entity,
                String.class
        );
    }

    private String encodeContent(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    private HttpEntity<Map<String, String>> createRequestEntity(GitHubUploadPayload gitHubUpload,
                                                                String encodedContent) {
        return new HttpEntity<>(
                createRequestBody(gitHubUpload.getCommitMessage(), encodedContent),
                createAuthorizationHeaders(gitHubUpload.getAccessToken())
        );
    }

    private Map<String, String> createRequestBody(String commitMessage, String encodedContent) {
        return Map.of(
                "message", commitMessage,
                "content", encodedContent
        );
    }

    private HttpHeaders createAuthorizationHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
        return headers;
    }
}
