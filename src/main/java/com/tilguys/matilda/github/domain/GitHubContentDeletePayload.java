package com.tilguys.matilda.github.domain;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDate;
import lombok.Getter;

public class GitHubContentDeletePayload {

    private static final String COMMIT_MESSAGE = "[TIL]: %s - Delete";
    private static final String STORAGE_API_URL_FORMAT = "https://api.github.com/repos/%s/%s/contents/til/%s.md";

    @Getter
    private final String accessToken;

    @Getter
    private final String deleteUrl;

    @Getter
    private final String commitMessage;

    @Getter
    private final String sha;

    public GitHubContentDeletePayload(GitHubStorage gitHubStorage, Til til, String sha) {
        this.accessToken = gitHubStorage.getAccessToken();
        this.deleteUrl = generateDeleteUrl(til.getTilUser().getIdentifier(), gitHubStorage.getRepositoryName(),
                til.getDate());
        this.commitMessage = String.format(COMMIT_MESSAGE, til.getTitle());
        this.sha = sha;
    }

    private String generateDeleteUrl(String identifier, String repositoryName, LocalDate date) {
        return String.format(STORAGE_API_URL_FORMAT,
                identifier,
                repositoryName,
                date);
    }
}
