package com.tilguys.matilda.github;

import static org.springframework.http.HttpHeaders.ACCEPT;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class GitHubHeaderRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final String GITHUB_API_VERSION_HEADER = "X-GitHub-Api-Version";
    private static final String GITHUB_MEDIA_TYPE = "application/vnd.github+json";
    private static final String GITHUB_API_VERSION = "2022-11-28";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        addGitHubApiHeaders(request.getHeaders());
        return execution.execute(request, body);
    }

    private void addGitHubApiHeaders(HttpHeaders headers) {
        headers.set(ACCEPT, GITHUB_MEDIA_TYPE);
        headers.add(GITHUB_API_VERSION_HEADER, GITHUB_API_VERSION);
    }
}
