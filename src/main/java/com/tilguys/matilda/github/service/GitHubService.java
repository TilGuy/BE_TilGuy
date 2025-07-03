package com.tilguys.matilda.github.service;

import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.github.client.GitHubClient;
import com.tilguys.matilda.github.controller.GitHubCredentialRequest;
import com.tilguys.matilda.github.domain.GitHubCredential;
import com.tilguys.matilda.github.domain.GitHubUpload;
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
    private final GitHubClient gitHubClient;
    private final GitHubCredentialRepository gitHubCredentialRepository;

    public void uploadTilToGitHub(Til til) {
        Optional<GitHubCredential> gitHubCredential = gitHubCredentialRepository.findByTilUserId(
                til.getTilUser().getId());
        if (isValidGitHubCredential(gitHubCredential)) {
            return;
        }

        gitHubClient.uploadTilContent(new GitHubUpload(gitHubCredential.get(), til));
    }

    @Transactional
    public void saveCredentials(long userId, GitHubCredentialRequest request) {
        Optional<GitHubCredential> optionalCredential = gitHubCredentialRepository.findByTilUserId(userId);

        if (optionalCredential.isPresent()) {
            updateCredential(optionalCredential.get(), request);
            return;
        }

        createCredential(userId, request);
    }

    private void createCredential(long userId, GitHubCredentialRequest request) {
        TilUser user = userService.getById(userId);
        GitHubCredential credential = GitHubCredential.builder()
                .tilUser(user)
                .accessToken(request.accessToken())
                .repositoryName(request.repositoryName())
                .isActivated(true)
                .build();
        gitHubCredentialRepository.save(credential);
    }

    private void updateCredential(GitHubCredential credential, GitHubCredentialRequest request) {
        credential.updateAccessTokenAndRepositoryName(request.accessToken(), request.repositoryName());
    }

    private boolean isValidGitHubCredential(Optional<GitHubCredential> gitHubCredential) {
        return gitHubCredential.isEmpty() || !gitHubCredential.get().isActivated();
    }
}
