package com.tilguys.matilda.github.controller;

public record GitHubCredentialRequest(
        String accessToken,
        String repositoryName
) {
    
}
