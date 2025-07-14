package com.tilguys.matilda.github.service;

import com.tilguys.matilda.common.auth.exception.MatildaException;
import com.tilguys.matilda.github.client.GitHubWorkflowClient;
import com.tilguys.matilda.github.domain.GitHubContentDeletePayload;
import com.tilguys.matilda.github.domain.GitHubContentGetPayload;
import com.tilguys.matilda.github.domain.GitHubContentUpdatePayload;
import com.tilguys.matilda.github.domain.GitHubContentUploadPayload;
import com.tilguys.matilda.github.domain.GitHubGetPayload;
import com.tilguys.matilda.github.domain.GitHubStorage;
import com.tilguys.matilda.github.repository.GitHubStorageRepository;
import com.tilguys.matilda.til.domain.Til;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubWorkflowService {

    private final GitHubWorkflowClient gitHubWorkflowClient;
    private final GitHubStorageRepository gitHubStorageRepository;

    public void uploadTilToGitHub(Til til) {
        processGitHubOperation(til, (storage) ->
                gitHubWorkflowClient.uploadContent(new GitHubContentUploadPayload(storage, til)));
    }

    public void updateTilToGitHub(Til til) {
        processGitHubOperation(til, (storage) -> {
            String sha = getSha(til, storage);
            gitHubWorkflowClient.updateContent(new GitHubContentUpdatePayload(storage, til, sha));
        });
    }

    public void deleteTilToGitHub(Til til) {
        processGitHubOperation(til, (storage) -> {
            String sha = getSha(til, storage);
            gitHubWorkflowClient.deleteContent(new GitHubContentDeletePayload(storage, til, sha));
        });
    }

    public void validateRepository(GitHubStorage gitHubStorage) {
        GitHubGetPayload getPayload = new GitHubGetPayload(gitHubStorage);

        try {
            gitHubWorkflowClient.getRepository(getPayload);
        } catch (Exception e) {
            throw new MatildaException("GitHub 저장소가 존재하지 않거나 활성화되지 않았습니다. \nToken 및 저장소 이름을 확인해주세요.");
        }
    }

    private void processGitHubOperation(Til til, Consumer<GitHubStorage> operation) {
        GitHubStorage gitHubStorage = getGitHubStorageIfValid(til);
        if (gitHubStorage == null) {
            return;
        }
        operation.accept(gitHubStorage);
    }

    private String getSha(Til til, GitHubStorage gitHubStorage) {
        GitHubContentGetPayload contentGetPayload = new GitHubContentGetPayload(gitHubStorage, til);
        Map<String, Object> content = gitHubWorkflowClient.getContent(contentGetPayload);
        return String.valueOf(content.get("sha"));
    }

    private GitHubStorage getGitHubStorageIfValid(Til til) {
        Optional<GitHubStorage> optionalStorage = gitHubStorageRepository.findByTilUserId(til.getTilUser().getId());

        if (optionalStorage.isEmpty() || !optionalStorage.get().isActivated()) {
            return null;
        }
        return optionalStorage.get();
    }
}
