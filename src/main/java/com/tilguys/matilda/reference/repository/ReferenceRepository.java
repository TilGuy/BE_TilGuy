package com.tilguys.matilda.reference.repository;

import com.tilguys.matilda.til.domain.Reference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferenceRepository extends JpaRepository<Reference, Long> {

    List<Reference> getAllByTil_TilId(Long tilId);
}
