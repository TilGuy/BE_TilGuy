package com.tilguys.matilda.user.repository;

import com.tilguys.matilda.user.TilUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<TilUser, Long> {

    Optional<TilUser> findByIdentifier(String identifier);

    List<TilUser> id(Long id);

    boolean existsByIdentifier(String identifier);
}
