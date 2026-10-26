package com.rentatools.infrastructure.rest.controller;

import com.rentatools.infrastructure.persistence.entity.CartItemEntity;
import com.rentatools.infrastructure.persistence.entity.ToolEntity;
import com.rentatools.infrastructure.persistence.repository.SpringDataCartItemRepository;
import com.rentatools.infrastructure.persistence.repository.ToolJpaRepository;
import com.rentatools.infrastructure.persistence.user.ProviderJpaEntity;
import com.rentatools.infrastructure.persistence.user.SpringDataProviderRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataUserRepository;
import com.rentatools.infrastructure.persistence.user.UserJpaEntity;
import com.rentatools.infrastructure.rest.dto.CartItemDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Carrito de Alquiler", description = "Endpoints para la persistencia del carrito en la base de datos (Cloud Cart)")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final SpringDataCartItemRepository cartItemRepository;
    private final SpringDataUserRepository userRepository;
    private final ToolJpaRepository toolJpaRepository;
    private final SpringDataProviderRepository providerRepository;

    public CartController(
            SpringDataCartItemRepository cartItemRepository,
            SpringDataUserRepository userRepository,
            ToolJpaRepository toolJpaRepository,
            SpringDataProviderRepository providerRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.toolJpaRepository = toolJpaRepository;
        this.providerRepository = providerRepository;
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        return userRepository.findByUsername(authentication.getName())
                .map(UserJpaEntity::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    @GetMapping
    @Operation(summary = "Obtener ítems del carrito del usuario autenticado")
    public ResponseEntity<List<CartItemDto.Response>> getCart(Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        List<CartItemEntity> entities = cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<CartItemDto.Response> response = entities.stream()
                .map(this::enrichCartItem)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Agregar maquinaria al carrito o actualizar si ya existe")
    public ResponseEntity<CartItemDto.Response> addToCart(
            @RequestBody CartItemDto.Request request,
            Authentication authentication
    ) {
        Long userId = getAuthenticatedUserId(authentication);
        if (request.toolId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toolId es obligatorio");
        }

        ToolEntity tool = toolJpaRepository.findById(request.toolId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maquinaria no encontrada"));

        int days = (request.days() != null && request.days() > 0) ? request.days() : 3;
        LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now().plusDays(1);
        LocalDate endDate = request.endDate() != null ? request.endDate() : startDate.plusDays(days);

        CartItemEntity entity = cartItemRepository.findByUserIdAndToolId(userId, tool.getId())
                .map(existing -> {
                    existing.setDays(days);
                    existing.setStartDate(startDate);
                    existing.setEndDate(endDate);
                    existing.setUpdatedAt(Instant.now());
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> {
                    CartItemEntity newItem = new CartItemEntity(userId, tool.getId(), days, startDate, endDate);
                    return cartItemRepository.save(newItem);
                });

        return ResponseEntity.status(HttpStatus.CREATED).body(enrichCartItem(entity, tool));
    }

    @PutMapping("/{toolId}")
    @Transactional
    @Operation(summary = "Actualizar días o fechas de una maquinaria en el carrito")
    public ResponseEntity<CartItemDto.Response> updateCartItem(
            @PathVariable Long toolId,
            @RequestBody CartItemDto.Request request,
            Authentication authentication
    ) {
        Long userId = getAuthenticatedUserId(authentication);
        CartItemEntity entity = cartItemRepository.findByUserIdAndToolId(userId, toolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ítem no encontrado en el carrito"));

        if (request.days() != null && request.days() > 0) {
            entity.setDays(request.days());
        }
        if (request.startDate() != null) {
            entity.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            entity.setEndDate(request.endDate());
        }
        entity.setUpdatedAt(Instant.now());
        CartItemEntity saved = cartItemRepository.save(entity);

        return ResponseEntity.ok(enrichCartItem(saved));
    }

    @DeleteMapping("/{toolId}")
    @Transactional
    @Operation(summary = "Eliminar una maquinaria del carrito")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long toolId, Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        cartItemRepository.deleteByUserIdAndToolId(userId, toolId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Transactional
    @Operation(summary = "Vaciar todo el carrito del usuario")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        cartItemRepository.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    private CartItemDto.Response enrichCartItem(CartItemEntity item) {
        ToolEntity tool = toolJpaRepository.findById(item.getToolId()).orElse(null);
        return enrichCartItem(item, tool);
    }

    private CartItemDto.Response enrichCartItem(CartItemEntity item, ToolEntity tool) {
        String name = tool != null ? tool.getName() : "Maquinaria #" + item.getToolId();
        String brand = tool != null ? tool.getBrand() : "CAT";
        String model = tool != null ? tool.getModel() : "Genérico";
        String code = tool != null ? tool.getCode() : "EQ-" + item.getToolId();
        String categoryName = (tool != null && tool.getCategory() != null) ? tool.getCategory().getName() : "Construcción";

        String providerName = "Proveedor Verificado";
        if (tool != null && tool.getProviderId() != null) {
            providerName = providerRepository.findById(tool.getProviderId())
                    .map(ProviderJpaEntity::getCompanyName)
                    .orElse("Proveedor Aliado");
        }

        String imageUrl = "assets/img/machinery/default.jpg";
        if (tool != null && tool.getImages() != null && !tool.getImages().isEmpty()) {
            imageUrl = tool.getImages().get(0).getImageUrl();
        }

        BigDecimal dailyPrice = tool != null ? tool.getDailyPrice() : BigDecimal.valueOf(100000);
        BigDecimal depositAmount = tool != null ? tool.getDepositAmount() : BigDecimal.valueOf(500000);

        int days = item.getDays() != null && item.getDays() > 0 ? item.getDays() : 1;
        BigDecimal subtotal = dailyPrice.multiply(BigDecimal.valueOf(days));
        BigDecimal iva = subtotal.multiply(BigDecimal.valueOf(0.19)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(depositAmount).add(iva);

        return new CartItemDto.Response(
                item.getId(),
                item.getUserId(),
                item.getToolId(),
                name,
                brand,
                model,
                code,
                imageUrl,
                categoryName,
                providerName,
                dailyPrice,
                depositAmount,
                days,
                item.getStartDate(),
                item.getEndDate(),
                subtotal,
                iva,
                depositAmount,
                total
        );
    }
}
