package com.tilguys.matilda.github;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitHubCredentialRepository extends JpaRepository<GitHubCredential, Long> {
    GitHubCredential findByTilUserId(Long tilUserId);
}
