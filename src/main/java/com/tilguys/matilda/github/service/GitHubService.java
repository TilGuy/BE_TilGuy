package com.tilguys.matilda.github.service;

import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.github.client.GitHubRepositoryClient;
import com.tilguys.matilda.github.controller.GitHubCredentialRequest;
import com.tilguys.matilda.github.domain.GitHubCommitPayload;
import com.tilguys.matilda.github.domain.GitHubRepository;
import com.tilguys.matilda.github.repository.GitHubCredentialRepository;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.user.TilUser;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final UserService userService;
    private final GitHubRepositoryClient gitHubClient;
    private final GitHubCredentialRepository gitHubCredentialRepository;

    public void uploadTilToGitHub(Til til) {
        Optional<GitHubRepository> gitHubCredential = gitHubCredentialRepository.findByTilUserId(
                til.getTilUser().getId());
        if (isValidGitHubCredential(gitHubCredential)) {
            return;
        }

        gitHubClient.uploadTilContent(new GitHubCommitPayload(gitHubCredential.get(), til));
    }

    @Transactional
    public void saveCredentials(long userId, GitHubCredentialRequest request) {
        Optional<GitHubRepository> optionalCredential = gitHubCredentialRepository.findByTilUserId(userId);

        if (optionalCredential.isPresent()) {
            updateCredential(optionalCredential.get(), request);
            return;
        }

        createCredential(userId, request);
    }

    private void createCredential(long userId, GitHubCredentialRequest request) {
        TilUser user = userService.getById(userId);
        GitHubRepository credential = GitHubRepository.builder()
                .tilUser(user)
                .accessToken(request.accessToken())
                .name(request.repositoryName())
                .isActivated(true)
                .build();
        gitHubCredentialRepository.save(credential);
    }

    private void updateCredential(GitHubRepository credential, GitHubCredentialRequest request) {
        credential.updateAccessTokenAndRepositoryName(request.accessToken(), request.repositoryName());
    }

    private boolean isValidGitHubCredential(Optional<GitHubRepository> gitHubCredential) {
        return gitHubCredential.isEmpty() || !gitHubCredential.get().isActivated();
    }
}
