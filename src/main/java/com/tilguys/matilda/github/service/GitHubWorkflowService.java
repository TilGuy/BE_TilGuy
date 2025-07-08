package com.tilguys.matilda.github.service;

import com.tilguys.matilda.github.client.GitHubStorageClient;
import com.tilguys.matilda.github.domain.GitHubStorage;
import com.tilguys.matilda.github.domain.GitHubUploadPayload;
import com.tilguys.matilda.github.repository.GitHubStorageRepository;
import com.tilguys.matilda.til.domain.Til;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubWorkflowService {

    private final GitHubStorageClient gitHubStorageClient;
    private final GitHubStorageRepository gitHubStorageRepository;

    public void uploadTilToGitHub(Til til) {
        Optional<GitHubStorage> optionalStorage = gitHubStorageRepository.findByTilUserId(
                til.getTilUser().getId());
        if (isValidGitHubStorage(optionalStorage)) {
            return;
        }

        gitHubStorageClient.uploadTilContent(new GitHubUploadPayload(optionalStorage.get(), til));
    }

    private boolean isValidGitHubStorage(Optional<GitHubStorage> storage) {
        return storage.isEmpty() || !storage.get().isActivated();
    }
}
