package com.tilguys.matilda.github.domain;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDate;
import lombok.Getter;

public class GitHubContentUpdatePayload {

    private static final String COMMIT_MESSAGE_PREFIX = "[TIL]: ";
    private static final String STORAGE_API_URL_FORMAT = "https://api.github.com/repos/%s/%s/contents/til/%s.md";
    private static final String CONTENT_TEMPLATE = """
            ### 🔗 [원본 URL](https://matilda.woowacourse.com/api/til/share/%d)
            
            ---
            
            # %s
            
            %s
            
            """;

    @Getter
    private final String accessToken;

    @Getter
    private final String updateUrl;

    @Getter
    private final String contents;

    @Getter
    private final String commitMessage;

    @Getter
    private final String sha;

    public GitHubContentUpdatePayload(GitHubStorage gitHubStorage, Til til, String sha) {
        this.accessToken = gitHubStorage.getAccessToken();
        this.updateUrl = generateUpdateUrl(til.getTilUser().getIdentifier(), gitHubStorage.getRepositoryName(),
                til.getDate());
        this.contents = generateContents(til.getTilId(), til.getTitle(), til.getContent());
        this.commitMessage = COMMIT_MESSAGE_PREFIX + til.getTitle();
        this.sha = sha;
    }

    private String generateUpdateUrl(String identifier, String repositoryName, LocalDate date) {
        return String.format(STORAGE_API_URL_FORMAT,
                identifier,
                repositoryName,
                date);
    }

    private String generateContents(Long tilId, String title, String contents) {
        return String.format(CONTENT_TEMPLATE,
                tilId,
                title,
                contents);
    }
}
