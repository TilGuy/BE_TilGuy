package com.tilguys.matilda.github.repository;

import com.tilguys.matilda.github.domain.GitHubCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitHubCredentialRepository extends JpaRepository<GitHubCredential, Long> {
    GitHubCredential findByTilUserId(Long tilUserId);
}
