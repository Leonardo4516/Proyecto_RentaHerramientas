package com.rentatools.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserJpaEntity u WHERE LOWER(u.email) = LOWER(:identifier) OR LOWER(u.email) LIKE LOWER(CONCAT(:identifier, '@%'))")
    List<UserJpaEntity> findByIdentifierMatches(@Param("identifier") String identifier);

    @Query("SELECT u FROM UserJpaEntity u JOIN u.roles r WHERE r.name = :roleName")
    List<UserJpaEntity> findByRoleName(@Param("roleName") String roleName);
}
