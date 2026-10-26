package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataCartItemRepository extends JpaRepository<CartItemEntity, Long> {

    List<CartItemEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CartItemEntity> findByUserIdAndToolId(Long userId, Long toolId);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndToolId(Long userId, Long toolId);
}
