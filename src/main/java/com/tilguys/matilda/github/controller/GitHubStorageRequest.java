package com.tilguys.matilda.github.controller;

public record GitHubStorageRequest(
        String accessToken,
        String repositoryName
) {

}
