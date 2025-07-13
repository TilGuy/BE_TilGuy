package com.tilguys.matilda.github.repository;

import com.tilguys.matilda.github.domain.GitHubStorage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitHubStorageRepository extends JpaRepository<GitHubStorage, Long> {
    Optional<GitHubStorage> findByTilUserId(Long tilUserId);
}
