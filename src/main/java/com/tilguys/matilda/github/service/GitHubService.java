package com.tilguys.matilda.github.service;

import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.github.client.GitHubStorageClient;
import com.tilguys.matilda.github.controller.GitHubStorageRequest;
import com.tilguys.matilda.github.domain.GitHubCommitPayload;
import com.tilguys.matilda.github.domain.GitHubStorage;
import com.tilguys.matilda.github.repository.GitHubStorageRepository;
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
    private final GitHubStorageClient gitHubStorageClient;
    private final GitHubStorageRepository gitHubStorageRepository;

    public void uploadTilToGitHub(Til til) {
        Optional<GitHubStorage> optionalStorage = gitHubStorageRepository.findByTilUserId(
                til.getTilUser().getId());
        if (isValidGitHubStorage(optionalStorage)) {
            return;
        }

        gitHubStorageClient.uploadTilContent(new GitHubCommitPayload(optionalStorage.get(), til));
    }

    @Transactional
    public void saveStorage(long userId, GitHubStorageRequest request) {
        Optional<GitHubStorage> optionalStorage = gitHubStorageRepository.findByTilUserId(userId);

        if (optionalStorage.isPresent()) {
            updateStorage(optionalStorage.get(), request);
            return;
        }
        createStorage(userId, request);
    }

    private void createStorage(long userId, GitHubStorageRequest request) {
        TilUser user = userService.getById(userId);
        GitHubStorage newStorage = GitHubStorage.builder()
                .tilUser(user)
                .accessToken(request.accessToken())
                .repositoryName(request.repositoryName())
                .isActivated(true)
                .build();
        gitHubStorageRepository.save(newStorage);
    }

    private void updateStorage(GitHubStorage storage, GitHubStorageRequest request) {
        storage.updateAccessTokenAndRepositoryName(request.accessToken(), request.repositoryName());
    }

    private boolean isValidGitHubStorage(Optional<GitHubStorage> storage) {
        return storage.isEmpty() || !storage.get().isActivated();
    }
}
