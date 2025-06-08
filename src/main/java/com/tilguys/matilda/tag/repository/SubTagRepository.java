package com.tilguys.matilda.tag.repository;

import com.tilguys.matilda.tag.domain.SubTag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubTagRepository extends JpaRepository<SubTag, Long> {

    List<SubTag> findByCreatedAtGreaterThanEqual(LocalDateTime recent);
}
