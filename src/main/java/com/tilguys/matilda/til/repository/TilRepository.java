package com.tilguys.matilda.til.repository;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TilRepository extends JpaRepository<Til, Long> {

    List<Til> findByUserId(final Long userId);

    Page<Til> findAllByUserId(final Pageable pageable, final Long userId);

    @Query(value = "SELECT t FROM Til t WHERE t.isDeleted = false AND t.isPublic = true ORDER BY t.createdAt DESC LIMIT 10")
    List<Til> findRecentPublicTils();

    @Query(value = "SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Til t WHERE t.isDeleted = false AND t.date = :date AND t.userId = :userId")
    boolean existsByDateAndUserId(@Param("date") LocalDate date, @Param("userId") Long userId);
}
