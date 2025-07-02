package com.tilguys.matilda.github;

import com.tilguys.matilda.til.domain.Til;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubUploadService {

    private final GitHubClient gitHubClient;
    private final GitHubCredentialRepository gitHubCredentialRepository;

    public void uploadTilToGitHub(Til til) {
        GitHubCredential gitHubCredential = gitHubCredentialRepository.findByTilUserId(til.getTilUser().getId());
        if (isValidGitHubCredential(gitHubCredential)) {
            return;
        }

        gitHubClient.uploadTilContent(new GitHubUpload(gitHubCredential, til));
    }

    private boolean isValidGitHubCredential(GitHubCredential gitHubCredential) {
        return gitHubCredential != null && gitHubCredential.isActivated();
    }
}
