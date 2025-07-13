package com.tilguys.matilda.github.service;

import com.tilguys.matilda.common.auth.exception.MatildaException;
import com.tilguys.matilda.github.client.GitHubStorageClient;
import com.tilguys.matilda.github.domain.GitHubGetPayload;
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

    public void validateRepository(GitHubStorage gitHubStorage) {
        GitHubGetPayload getPayload = new GitHubGetPayload(gitHubStorage);

        try {
            gitHubStorageClient.getRepository(getPayload);
        } catch (Exception e) {
            throw new MatildaException("GitHub 저장소가 존재하지 않거나 활성화되지 않았습니다. \nToken 및 저장소 이름을 확인해주세요.");
        }
    }

    private boolean isValidGitHubStorage(Optional<GitHubStorage> storage) {
        return storage.isEmpty() || !storage.get().isActivated();
    }
}
