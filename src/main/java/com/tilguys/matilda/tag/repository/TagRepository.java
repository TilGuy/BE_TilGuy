package com.tilguys.matilda.tag.repository;

import com.tilguys.matilda.til.domain.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByCreatedAtGreaterThanEqual(LocalDateTime recent);
}
