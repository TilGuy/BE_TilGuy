package com.tilguys.matilda.github.repository;

import com.tilguys.matilda.github.domain.GitHubRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitHubCredentialRepository extends JpaRepository<GitHubRepository, Long> {
    Optional<GitHubRepository> findByTilUserId(Long tilUserId);
}
