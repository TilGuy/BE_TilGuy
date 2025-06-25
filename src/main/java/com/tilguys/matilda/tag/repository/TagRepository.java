package com.tilguys.matilda.tag.repository;

import com.tilguys.matilda.til.domain.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("""
                SELECT t FROM Tag t
                JOIN FETCH t.til
                WHERE t.createdAt >= :start
            """)
    List<Tag> findByCreatedAtGreaterThanEqual(@Param("start") LocalDateTime recent);
}
