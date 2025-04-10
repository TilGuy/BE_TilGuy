package com.tilguys.matilda.til.repository;

import com.tilguys.matilda.til.domain.Til;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TilRepository extends JpaRepository<Til, Long> {
    List<Til> findByUserId(final Long userId);
}
