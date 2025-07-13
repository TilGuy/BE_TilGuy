package com.tilguys.matilda.github.service;

import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.github.controller.GitHubStorageRequest;
import com.tilguys.matilda.github.domain.GitHubStorage;
import com.tilguys.matilda.github.repository.GitHubStorageRepository;
import com.tilguys.matilda.user.TilUser;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GitHubStorageService {

    private final UserService userService;
    private final GitHubWorkflowService gitHubWorkflowService;
    private final GitHubStorageRepository gitHubStorageRepository;

    @Transactional
    public void saveStorage(long userId, GitHubStorageRequest request) {
        Optional<GitHubStorage> optionalStorage = gitHubStorageRepository.findByTilUserId(userId);
        GitHubStorage gitHubStorage = creatOrUpdate(userId, request, optionalStorage);
        gitHubWorkflowService.validateRepository(gitHubStorage);
    }

    private GitHubStorage creatOrUpdate(long userId, GitHubStorageRequest request,
                                        Optional<GitHubStorage> optionalStorage) {
        if (optionalStorage.isPresent()) {
            updateStorage(optionalStorage.get(), request);
            return optionalStorage.get();
        }
        return createStorage(userId, request);
    }

    private GitHubStorage createStorage(long userId, GitHubStorageRequest request) {
        TilUser user = userService.getById(userId);
        GitHubStorage newStorage = GitHubStorage.builder()
                .tilUser(user)
                .accessToken(request.accessToken())
                .repositoryName(request.repositoryName())
                .isActivated(true)
                .build();
        return gitHubStorageRepository.save(newStorage);
    }

    private void updateStorage(GitHubStorage storage, GitHubStorageRequest request) {
        storage.updateAccessTokenAndRepositoryName(request.accessToken(), request.repositoryName());
    }
}
