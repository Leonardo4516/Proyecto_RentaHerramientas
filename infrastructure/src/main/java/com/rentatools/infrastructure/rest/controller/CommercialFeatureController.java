package com.rentatools.infrastructure.rest.controller;

import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.infrastructure.persistence.entity.PromoCouponEntity;
import com.rentatools.infrastructure.persistence.entity.ToolReviewEntity;
import com.rentatools.infrastructure.persistence.repository.RentalJpaRepository;
import com.rentatools.infrastructure.persistence.repository.SpringDataPromoCouponRepository;
import com.rentatools.infrastructure.persistence.repository.SpringDataToolReviewRepository;
import com.rentatools.infrastructure.persistence.repository.ToolJpaRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataClientRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Funcionalidades Comerciales", description = "Endpoints para cupones promocionales de descuento y calificaciones/reseñas de maquinaria")
public class CommercialFeatureController {

    private final SpringDataPromoCouponRepository couponRepository;
    private final SpringDataToolReviewRepository reviewRepository;
    private final RentalJpaRepository rentalRepository;
    private final ToolJpaRepository toolRepository;
    private final SpringDataClientRepository clientRepository;
    private final SpringDataUserRepository userRepository;

    public CommercialFeatureController(SpringDataPromoCouponRepository couponRepository,
                                       SpringDataToolReviewRepository reviewRepository,
                                       RentalJpaRepository rentalRepository,
                                       ToolJpaRepository toolRepository,
                                       SpringDataClientRepository clientRepository,
                                       SpringDataUserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.reviewRepository = reviewRepository;
        this.rentalRepository = rentalRepository;
        this.toolRepository = toolRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/coupons/validate")
    @Operation(summary = "Validar cupón promocional", description = "Verifica la vigencia y porcentaje de descuento de un código de cupón")
    public ResponseEntity<Map<String, Object>> validateCoupon(@RequestParam String code) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "El código de cupón no puede estar vacío"));
        }

        var couponOpt = couponRepository.findByCodeIgnoreCaseAndIsActiveTrue(code.trim());
        if (couponOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "code", code.toUpperCase(),
                    "valid", false,
                    "discountPercentage", BigDecimal.ZERO,
                    "message", "El cupón ingresado no existe o no se encuentra activo."
            ));
        }

        PromoCouponEntity coupon = couponOpt.get();
        if (coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDate.now())) {
            return ResponseEntity.ok(Map.of(
                    "code", coupon.getCode(),
                    "valid", false,
                    "discountPercentage", BigDecimal.ZERO,
                    "message", "El cupón ha expirado el " + coupon.getValidUntil()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "code", coupon.getCode(),
                "valid", true,
                "discountPercentage", coupon.getDiscountPercentage(),
                "maxDiscountAmount", coupon.getMaxDiscountAmount() != null ? coupon.getMaxDiscountAmount() : BigDecimal.ZERO,
                "message", "¡Cupón " + coupon.getCode() + " de " + coupon.getDiscountPercentage() + "% aplicado exitosamente!"
        ));
    }

    @GetMapping("/coupons")
    @Operation(summary = "Listar cupones disponibles", description = "Retorna los cupones activos de la plataforma")
    public ResponseEntity<List<PromoCouponEntity>> listCoupons() {
        return ResponseEntity.ok(couponRepository.findAll().stream().filter(c -> Boolean.TRUE.equals(c.getIsActive())).toList());
    }

    @PostMapping("/reviews")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN')")
    @Operation(summary = "Publicar reseña y calificación de maquinaria", description = "Permite a un cliente calificar una herramienta tras completar su alquiler (1 a 5 estrellas)")
    public ResponseEntity<Map<String, Object>> createReview(
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        Long toolId = body.get("toolId") != null ? Long.valueOf(body.get("toolId").toString()) : null;
        Long rentalId = body.get("rentalId") != null ? Long.valueOf(body.get("rentalId").toString()) : null;
        Integer rating = body.get("rating") != null ? Integer.valueOf(body.get("rating").toString()) : 5;
        String comment = body.get("comment") != null ? body.get("comment").toString() : "";

        if (toolId == null) {
            throw new BusinessRuleValidationException("El ID de la herramienta es requerido para la reseña.");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessRuleValidationException("La calificación debe estar entre 1 y 5 estrellas.");
        }

        if (!toolRepository.existsById(toolId)) {
            throw new EntityNotFoundException("Herramienta no encontrada con ID: " + toolId);
        }

        Long resolvedClientId = 1L;
        if (authentication != null && authentication.isAuthenticated()) {
            var userOpt = userRepository.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                var cOpt = clientRepository.findByUserId(userOpt.get().getId());
                resolvedClientId = cOpt.map(c -> c.getId()).orElse(userOpt.get().getId());
            }
        }

        if (rentalId != null) {
            var rOpt = rentalRepository.findById(rentalId);
            if (rOpt.isPresent() && !"COMPLETADA".equals(rOpt.get().getStatus())) {
                throw new BusinessRuleValidationException("Solo se pueden calificar alquileres que hayan finalizado con devolución completa.");
            }
        }

        ToolReviewEntity review = new ToolReviewEntity(rentalId, toolId, resolvedClientId, rating, comment);
        ToolReviewEntity saved = reviewRepository.save(review);

        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "toolId", saved.getToolId(),
                "rentalId", saved.getRentalId() != null ? saved.getRentalId() : 0,
                "rating", saved.getRating(),
                "comment", saved.getComment() != null ? saved.getComment() : "",
                "createdAt", saved.getCreatedAt(),
                "message", "¡Reseña y calificación registradas exitosamente!"
        ));
    }

    @GetMapping("/reviews/tool/{toolId}")
    @Operation(summary = "Listar reseñas de una herramienta", description = "Retorna las valoraciones y comentarios de clientes para un equipo")
    public ResponseEntity<List<ToolReviewEntity>> getToolReviews(@PathVariable Long toolId) {
        return ResponseEntity.ok(reviewRepository.findByToolIdOrderByCreatedAtDesc(toolId));
    }
}
