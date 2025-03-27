package com.tilguys.matilda.user.repository;

import com.tilguys.matilda.user.entity.ProviderInfo;
import com.tilguys.matilda.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.identifier = :identifier and u.providerInfo = :providerInfo")
    Optional<User> findByOAuthInfo(@Param("identifier") String identifier,
                                   @Param("providerInfo") ProviderInfo providerInfo);

    Optional<User> findByIdentifier(String identifier);
}
