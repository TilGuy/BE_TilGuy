package com.tilguys.matilda.github.domain;

import lombok.Getter;

public class GitHubGetPayload {

    private static final String STORAGE_API_URL_FORMAT = "https://api.github.com/repos/%s/%s";

    @Getter
    private final String accessToken;

    @Getter
    private final String getUrl;

    public GitHubGetPayload(GitHubStorage gitHubStorage) {
        this.accessToken = gitHubStorage.getAccessToken();
        this.getUrl = generateGetUrl(gitHubStorage.getTilUser().getIdentifier(), gitHubStorage.getRepositoryName());
    }

    private String generateGetUrl(String identifier, String repositoryName) {
        return String.format(STORAGE_API_URL_FORMAT,
                identifier,
                repositoryName);
    }
}
