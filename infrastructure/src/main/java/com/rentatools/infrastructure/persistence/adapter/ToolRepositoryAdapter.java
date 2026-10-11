package com.rentatools.infrastructure.persistence.adapter;

import com.rentatools.domain.tool.*;
import com.rentatools.infrastructure.persistence.entity.ToolCategoryEntity;
import com.rentatools.infrastructure.persistence.entity.ToolEntity;
import com.rentatools.infrastructure.persistence.entity.ToolImageEntity;
import com.rentatools.infrastructure.persistence.entity.ToolSpecificationEntity;
import com.rentatools.infrastructure.persistence.repository.ToolCategoryJpaRepository;
import com.rentatools.infrastructure.persistence.repository.ToolJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ToolRepositoryAdapter implements ToolRepository {

    private final ToolJpaRepository toolJpaRepository;
    private final ToolCategoryJpaRepository categoryJpaRepository;

    public ToolRepositoryAdapter(ToolJpaRepository toolJpaRepository, ToolCategoryJpaRepository categoryJpaRepository) {
        this.toolJpaRepository = toolJpaRepository;
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    @Transactional
    public Tool save(Tool domain) {
        ToolCategoryEntity categoryEntity = categoryJpaRepository.findById(domain.getCategoryId())
                .orElse(null);

        ToolEntity entity = toolJpaRepository.findById(domain.getId()).orElse(new ToolEntity());
        entity.setId(domain.getId());
        entity.setProviderId(domain.getProviderId());
        entity.setCategory(categoryEntity);
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setBrand(domain.getBrand());
        entity.setModel(domain.getModel());
        entity.setDailyPrice(domain.getDailyPrice());
        entity.setDepositAmount(domain.getDepositAmount());
        entity.setStatus(domain.getStatus().name());
        entity.setAccumulatedRentalDays(domain.getAccumulatedRentalDays());
        if (domain.getCoordinates() != null) {
            entity.setLat(domain.getCoordinates().latitude());
            entity.setLng(domain.getCoordinates().longitude());
        }
        entity.setCreatedAt(domain.getCreatedAt());

        // Manejar imágenes
        entity.getImages().clear();
        for (ToolImage img : domain.getImages()) {
            entity.getImages().add(new ToolImageEntity(img.getId(), entity, img.getImageUrl(), img.isCover()));
        }

        // Manejar especificaciones
        entity.getSpecifications().clear();
        for (ToolSpecification spec : domain.getSpecifications()) {
            entity.getSpecifications().add(new ToolSpecificationEntity(spec.getId(), entity, spec.getSpecName(), spec.getSpecValue()));
        }

        ToolEntity saved = toolJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tool> findById(UUID id) {
        return toolJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tool> findByCode(String code) {
        return toolJpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tool> findAll() {
        return toolJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tool> findByCategoryId(UUID categoryId) {
        return toolJpaRepository.findByCategoryId(categoryId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tool> findByProviderId(UUID providerId) {
        return toolJpaRepository.findByProviderId(providerId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tool> findByStatus(ToolStatus status) {
        return toolJpaRepository.findByStatus(status.name()).stream().map(this::toDomain).toList();
    }

    private Tool toDomain(ToolEntity e) {
        GeoCoordinates coords = (e.getLat() != null && e.getLng() != null)
                ? new GeoCoordinates(e.getLat(), e.getLng())
                : null;

        List<ToolImage> images = e.getImages().stream()
                .map(i -> new ToolImage(i.getId(), i.getImageUrl(), i.isCover()))
                .toList();

        List<ToolSpecification> specs = e.getSpecifications().stream()
                .map(s -> new ToolSpecification(s.getId(), s.getSpecName(), s.getSpecValue()))
                .toList();

        ToolStatus status = ToolStatus.valueOf(e.getStatus());

        return new Tool(
                e.getId(),
                e.getProviderId(),
                e.getCategory() != null ? e.getCategory().getId() : null,
                e.getCode(),
                e.getName(),
                e.getBrand(),
                e.getModel(),
                e.getDailyPrice(),
                e.getDepositAmount(),
                status,
                e.getAccumulatedRentalDays(),
                coords,
                e.getCreatedAt(),
                images,
                specs
        );
    }
}
