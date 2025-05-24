package com.tilguys.matilda.til.repository;

import com.tilguys.matilda.til.domain.Til;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TilRepository extends JpaRepository<Til, Long> {

    List<Til> findAllByOrderByDateDesc();

    List<Til> findByTilUserId(final Long userId);

    Page<Til> findAllByTilUserId(final Pageable pageable, final Long userId);

    List<Til> findTop10ByIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc();

    boolean existsByDateAndUserIdAndIsDeletedFalse(LocalDate date, Long userId);
}
