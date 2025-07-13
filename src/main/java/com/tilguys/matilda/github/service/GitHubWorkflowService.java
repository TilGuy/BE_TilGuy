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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubWorkflowService {

    private final GitHubWorkflowClient gitHubWorkflowClient;
    private final GitHubStorageRepository gitHubStorageRepository;

    public void uploadTilToGitHub(Til til) {
        Optional<GitHubStorage> optionalStorage = gitHubStorageRepository.findByTilUserId(
                til.getTilUser().getId());

        if (isValidGitHubStorage(optionalStorage)) {
            return;
        }
        gitHubWorkflowClient.uploadContent(new GitHubContentUploadPayload(optionalStorage.get(), til));
    }

    public void updateTilToGitHub(Til til) {
        Optional<GitHubStorage> optionalStorage = gitHubStorageRepository.findByTilUserId(
                til.getTilUser().getId());

        if (isValidGitHubStorage(optionalStorage)) {
            return;
        }

        GitHubContentGetPayload contentGetPayload = new GitHubContentGetPayload(optionalStorage.get(), til);
        Map<String, Object> content = gitHubWorkflowClient.getContent(contentGetPayload);
        String sha = String.valueOf(content.get("sha"));

        gitHubWorkflowClient.updateContent(new GitHubContentUpdatePayload(optionalStorage.get(), til, sha));
    }

    public void deleteTilToGitHub(Til til) {
        Optional<GitHubStorage> optionalStorage = gitHubStorageRepository.findByTilUserId(
                til.getTilUser().getId());

        if (isValidGitHubStorage(optionalStorage)) {
            return;
        }

        GitHubContentGetPayload contentGetPayload = new GitHubContentGetPayload(optionalStorage.get(), til);
        Map<String, Object> content = gitHubWorkflowClient.getContent(contentGetPayload);
        String sha = String.valueOf(content.get("sha"));

        gitHubWorkflowClient.deleteContent(new GitHubContentDeletePayload(optionalStorage.get(), til, sha));
    }

    public void validateRepository(GitHubStorage gitHubStorage) {
        GitHubGetPayload getPayload = new GitHubGetPayload(gitHubStorage);

        try {
            gitHubWorkflowClient.getRepository(getPayload);
        } catch (Exception e) {
            throw new MatildaException("GitHub 저장소가 존재하지 않거나 활성화되지 않았습니다. \nToken 및 저장소 이름을 확인해주세요.");
        }
    }

    private boolean isValidGitHubStorage(Optional<GitHubStorage> storage) {
        return storage.isEmpty() || !storage.get().isActivated();
    }
}
