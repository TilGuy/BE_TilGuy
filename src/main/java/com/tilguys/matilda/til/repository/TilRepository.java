package com.tilguys.matilda.til.repository;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TilRepository extends JpaRepository<Til, Long> {

    @EntityGraph(attributePaths = {"tilUser"})
    Page<Til> findAllByIsPublicTrueAndIsDeletedFalse(final Pageable pageable);

    List<Til> findByTilUserId(final Long userId);

    List<Til> findAllByTilUserIdAndDateBetweenAndIsDeleted(Long tilUserId, LocalDate dateAfter, LocalDate dateBefore,
                                                           boolean deleted);

    boolean existsByDateAndTilUserIdAndIsDeletedFalse(LocalDate date, Long userId);

    List<Til> findByCreatedAtGreaterThanEqual(LocalDateTime recent);

    @EntityGraph(attributePaths = {"tilUser"})
    @Query("SELECT t FROM Til t " +
            "WHERE t.isDeleted = false AND t.isPublic = true " +
            "AND (:cursorDate IS NULL OR t.createdAt < :cursorDate " +
            "    OR (t.createdAt = :cursorDate AND t.tilId < :cursorId)) " +
            "ORDER BY t.createdAt DESC, t.tilId DESC")
    List<Til> findPublicTilsWithAllInfo(@Param("cursorDate") LocalDateTime cursorDate,
                                        @Param("cursorId") Long cursorId,
                                        Pageable pageable);


    Optional<Til> findByTilIdAndIsDeletedFalse(Long tilId);
}
