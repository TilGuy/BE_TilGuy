package com.tilguys.matilda.github.client;

import com.tilguys.matilda.github.domain.GitHubContentDeletePayload;
import com.tilguys.matilda.github.domain.GitHubContentGetPayload;
import com.tilguys.matilda.github.domain.GitHubContentUpdatePayload;
import com.tilguys.matilda.github.domain.GitHubContentUploadPayload;
import com.tilguys.matilda.github.domain.GitHubGetPayload;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GitHubWorkflowClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RestTemplate restTemplate;

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

    public Map<String, Object> getContent(GitHubContentGetPayload contentGetPayload) {
        HttpHeaders headers = createAuthorizationHeaders(contentGetPayload.getAccessToken());
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                contentGetPayload.getGetUrl(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody();
    }

    public void uploadContent(GitHubContentUploadPayload contentUploadPayload) {
        String encodedContent = encodeContent(contentUploadPayload.getContents());
        HttpEntity<Map<String, String>> requestEntity = createUploadRequestEntity(contentUploadPayload, encodedContent);
        restTemplate.put(contentUploadPayload.getUploadUrl(), requestEntity);
    }

    public void updateContent(GitHubContentUpdatePayload contentUpdatePayload) {
        String encodedContent = encodeContent(contentUpdatePayload.getContents());
        HttpEntity<Map<String, String>> requestEntity = createUpdateRequestEntity(contentUpdatePayload, encodedContent);
        restTemplate.put(contentUpdatePayload.getUpdateUrl(), requestEntity);
    }

    public void deleteContent(GitHubContentDeletePayload contentDeletePayload) {
        HttpEntity<Map<String, String>> requestEntity = createDeleteRequestEntity(contentDeletePayload);
        restTemplate.exchange(
                contentDeletePayload.getDeleteUrl(),
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );
    }

    private String encodeContent(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    private HttpEntity<Map<String, String>> createUploadRequestEntity(GitHubContentUploadPayload contentUploadPayload,
                                                                      String encodedContent) {
        return new HttpEntity<>(
                createUploadRequestBody(contentUploadPayload.getCommitMessage(), encodedContent),
                createAuthorizationHeaders(contentUploadPayload.getAccessToken())
        );
    }

    private Map<String, String> createUploadRequestBody(String commitMessage, String encodedContent) {
        return Map.of(
                "message", commitMessage,
                "content", encodedContent
        );
    }

    private HttpEntity<Map<String, String>> createUpdateRequestEntity(GitHubContentUpdatePayload contentUpdatePayload,
                                                                      String encodedContent) {
        return new HttpEntity<>(
                createUpdateRequestBody(contentUpdatePayload.getCommitMessage(), encodedContent,
                        contentUpdatePayload.getSha()),
                createAuthorizationHeaders(contentUpdatePayload.getAccessToken())
        );
    }

    private Map<String, String> createUpdateRequestBody(String commitMessage, String encodedContent, String sha) {
        return Map.of(
                "message", commitMessage,
                "content", encodedContent,
                "sha", sha
        );
    }

    private HttpEntity<Map<String, String>> createDeleteRequestEntity(GitHubContentDeletePayload contentDeletePayload) {
        return new HttpEntity<>(
                createDeleteRequestBody(contentDeletePayload.getCommitMessage(), contentDeletePayload.getSha()),
                createAuthorizationHeaders(contentDeletePayload.getAccessToken())
        );
    }

    private Map<String, String> createDeleteRequestBody(String commitMessage, String sha) {
        return Map.of(
                "message", commitMessage,
                "sha", sha
        );
    }

    private HttpHeaders createAuthorizationHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
        return headers;
    }
}
