package com.tilguys.matilda.tag.repository;

import com.tilguys.matilda.tag.domain.SubTag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubTagRepository extends JpaRepository<SubTag, Long> {

    @Query("""
                SELECT st FROM SubTag st
                JOIN FETCH st.tag t
                JOIN FETCH t.til
                WHERE st.createdAt >= :start
            """)
    List<SubTag> findByCreatedAtGreaterThanEqual(@Param("start") LocalDateTime recent);
}
