package com.tilguys.matilda.github.domain;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDate;
import lombok.Getter;

public class GitHubCommitPayload {

    private static final String COMMIT_MESSAGE_PREFIX = "[TIL]: ";
    private static final String REPOSITORY_API_URL_FORMAT = "https://api.github.com/repos/%s/%s/contents/til/%s.md";
    private static final String CONTENT_TEMPLATE = """
            ### 🔗 [원본 URL](https://matilda.woowacourse.com/api/til/share/%d)
            
            ---
            
            # %s
            
            %s
            
            """;

    @Getter
    private final String accessToken;

    @Getter
    private final String uploadsUrl;

    @Getter
    private final String contents;

    @Getter
    private final String commitMessage;

    public GitHubCommitPayload(GitHubRepository gitHubRepository, Til til) {
        this.accessToken = gitHubRepository.getAccessToken();
        this.uploadsUrl = generateUploadUrl(til.getTilUser().getIdentifier(), gitHubRepository.getName(),
                til.getDate());
        this.contents = generateContents(til.getTilId(), til.getTitle(), til.getContent());
        this.commitMessage = COMMIT_MESSAGE_PREFIX + til.getTitle();
    }

    private String generateUploadUrl(String identifier, String repositoryName, LocalDate date) {
        return String.format(REPOSITORY_API_URL_FORMAT,
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
