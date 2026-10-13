package com.rentatools.infrastructure.persistence.adapter;

import com.rentatools.domain.tool.ToolCategory;
import com.rentatools.domain.tool.ToolCategoryRepository;
import com.rentatools.infrastructure.persistence.entity.ToolCategoryEntity;
import com.rentatools.infrastructure.persistence.repository.ToolCategoryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
@Component
public class ToolCategoryRepositoryAdapter implements ToolCategoryRepository {
    private final ToolCategoryJpaRepository jpaRepository;

    public ToolCategoryRepositoryAdapter(ToolCategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ToolCategory save(ToolCategory category) {
        ToolCategoryEntity entity = new ToolCategoryEntity(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIconName(),
                category.isActive()
        );
        ToolCategoryEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ToolCategory> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ToolCategory> findAllActive() {
        return jpaRepository.findByActiveTrue().stream().map(this::toDomain).toList();
    }

    private ToolCategory toDomain(ToolCategoryEntity entity) {
        return new ToolCategory(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getIconName(),
                entity.isActive()
        );
    }
}
