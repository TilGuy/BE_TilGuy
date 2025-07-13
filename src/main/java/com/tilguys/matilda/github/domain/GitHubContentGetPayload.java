package com.tilguys.matilda.github.domain;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDate;
import lombok.Getter;

public class GitHubContentGetPayload {

    private static final String STORAGE_API_URL_FORMAT = "https://api.github.com/repos/%s/%s/contents/til/%s.md";

    @Getter
    private final String accessToken;

    @Getter
    private final String getUrl;

    public GitHubContentGetPayload(GitHubStorage gitHubStorage, Til til) {
        this.accessToken = gitHubStorage.getAccessToken();
        this.getUrl = generateGetUrl(
                gitHubStorage.getTilUser().getIdentifier(),
                gitHubStorage.getRepositoryName(),
                til.getDate()
        );
    }

    private String generateGetUrl(String identifier, String repositoryName, LocalDate date) {
        return String.format(STORAGE_API_URL_FORMAT,
                identifier,
                repositoryName,
                date);
    }
}
