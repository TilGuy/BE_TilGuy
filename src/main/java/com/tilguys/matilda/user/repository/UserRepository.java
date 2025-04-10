package com.tilguys.matilda.user.repository;

import com.tilguys.matilda.user.TilUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<TilUser, Long> {

    Optional<TilUser> findByIdentifier(String identifier);
}
