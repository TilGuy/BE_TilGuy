package com.tilguys.matilda.til.repository;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TilRepository extends JpaRepository<Til, Long> {

    Page<Til> findAllByIsPublicTrueAndIsDeletedFalse(final Pageable pageable);

    List<Til> findByTilUserId(final Long userId);

    boolean existsByDateAndTilUserIdAndIsDeletedFalse(LocalDate date, Long userId);

    List<Til> findByCreatedAtGreaterThanEqual(LocalDateTime recent);
}
