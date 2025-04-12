package com.tilguys.matilda.til.repository;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TilRepository extends JpaRepository<Til, Long> {
    List<Til> findByUserId(final Long userId);

    List<Til> findByCreatedAtBetween(final LocalDateTime from, final LocalDateTime to);

    Page<Til> findAllByUserId(final Pageable pageable, final Long userId);
}
