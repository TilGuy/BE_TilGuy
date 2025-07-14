package com.tilguys.matilda.github.dto;

import com.tilguys.matilda.github.domain.GitHubStorage;

public record GitHubStorageResponse(
        String repositoryName,
        boolean isActivated
) {

    public GitHubStorageResponse(GitHubStorage gitHubStorage) {
        this(
                gitHubStorage.getRepositoryName(),
                gitHubStorage.isActivated()
        );
    }
}
