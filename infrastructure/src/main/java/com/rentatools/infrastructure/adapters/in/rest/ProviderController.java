package com.rentatools.infrastructure.adapters.in.rest;

import com.rentatools.application.auth.dto.ProviderDirectoryResponse;
import com.rentatools.infrastructure.persistence.repository.ToolJpaRepository;
import com.rentatools.infrastructure.persistence.user.ProviderJpaEntity;
import com.rentatools.infrastructure.persistence.user.SpringDataProviderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/providers")
@Tag(name = "Proveedores", description = "Directorio de empresas proveedoras de maquinaria y verificación administrativa")
public class ProviderController {

    private final SpringDataProviderRepository providerRepo;
    private final ToolJpaRepository toolRepo;

    public ProviderController(SpringDataProviderRepository providerRepo, ToolJpaRepository toolRepo) {
        this.providerRepo = providerRepo;
        this.toolRepo = toolRepo;
    }

    @GetMapping
    @Operation(summary = "Listar directorio de proveedores", description = "Retorna empresas aliadas con conteo de maquinaria en flota y filtro de verificación")
    public ResponseEntity<List<ProviderDirectoryResponse>> getAllProviders(
            @RequestParam(required = false) Boolean verified) {
        List<ProviderJpaEntity> providers = (verified != null)
                ? providerRepo.findByVerified(verified)
                : providerRepo.findAll();

        List<ProviderDirectoryResponse> response = providers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar proveedor por ID", description = "Obtiene perfil de la empresa proveedora, rating y tamaño de inventario")
    public ResponseEntity<ProviderDirectoryResponse> getProviderById(@PathVariable Long id) {
        return providerRepo.findById(id)
                .or(() -> providerRepo.findByUserId(id))
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Verificar / Homologar proveedor", description = "Exclusivo de Administrador: aprueba u homologa una empresa proveedora para ofrecer maquinaria")
    public ResponseEntity<ProviderDirectoryResponse> verifyProvider(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "true") Boolean verified) {
        ProviderJpaEntity provider = providerRepo.findById(id)
                .or(() -> providerRepo.findByUserId(id))
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + id));

        provider.setVerified(verified);
        ProviderJpaEntity saved = providerRepo.save(provider);
        return ResponseEntity.ok(toResponse(saved));
    }

    private ProviderDirectoryResponse toResponse(ProviderJpaEntity p) {
        int fleetCount = toolRepo.findByProviderId(p.getId()).size();
        return new ProviderDirectoryResponse(
                p.getId(),
                p.getUserId(),
                p.getCompanyName(),
                p.getTaxId(),
                p.getCommercialRegistry(),
                p.getCommercialAddress(),
                p.getCity(),
                p.getRating(),
                p.getVerified(),
                fleetCount,
                p.getCreatedAt()
        );
    }
}
