package com.tilguys.matilda.tag.repository;

import com.tilguys.matilda.tag.domain.TagRelation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRelationRepository extends JpaRepository<TagRelation, Long> {

    List<TagRelation> findByCreatedAtGreaterThanEqual(LocalDateTime recent);

    Optional<TagRelation> findByTagId(Long id);
}
