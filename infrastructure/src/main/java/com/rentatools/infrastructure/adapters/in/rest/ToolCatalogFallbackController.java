package com.rentatools.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Maquinaria & Reservas (Fallback Dev A)", description = "Catálogo de herramientas, búsqueda geográfica y gestión de reservas")
public class ToolCatalogFallbackController {

    private static final List<Map<String, Object>> TOOLS = new CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> RENTALS = new CopyOnWriteArrayList<>();

    static {
        // Semillas iniciales de maquinaria pesada y equipos
        TOOLS.add(createTool(1L, "Retroexcavadora CAT 420F2", "Maquinaria Pesada", "Motor C4.4 ACERT 93 hp, tracción 4x4, brazo extensible y cucharón frontal 1m³.", new BigDecimal("450000"), new BigDecimal("1200000"), 4.6097, -74.0817, "Bogotá Norte", "DISPONIBLE", "https://images.unsplash.com/photo-1579781354186-012d7cadb2b1?w=800&auto=format&fit=crop"));
        TOOLS.add(createTool(2L, "Torre de Iluminación Generac 6kW", "Generación e Iluminación", "4 lámparas LED 350W alta eficiencia, mástil telescópico 9m y autonomía de 60h.", new BigDecimal("200000"), new BigDecimal("500000"), 4.6534, -74.0543, "Bogotá Chapinero", "DISPONIBLE", "https://images.unsplash.com/photo-1581094288338-2314dddb7ece?w=800&auto=format&fit=crop"));
        TOOLS.add(createTool(3L, "Mezcladora de Concreto Diésel 2 Bultos", "Concreto y Mezcla", "Capacidad 380L, motor Yanmar 10HP, chasis reforzado de arrastre pesado.", new BigDecimal("90000"), new BigDecimal("300000"), 4.6300, -74.1100, "Bogotá Fontibón", "DISPONIBLE", "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800&auto=format&fit=crop"));
        TOOLS.add(createTool(4L, "Andamio Certificado Multidireccional (Kit 20m²)", "Alturas y Seguridad", "Acero galvanizado en caliente con certificación europea EN 12810. Incluye plataformas y barandas.", new BigDecimal("60000"), new BigDecimal("250000"), 4.7110, -74.0300, "Bogotá Usaquén", "DISPONIBLE", "https://images.unsplash.com/photo-1541888946425-d0fbb186c5f7?w=800&auto=format&fit=crop"));
        TOOLS.add(createTool(5L, "Taladro Demoledor Bosch GSH 16-28 (HEX 28mm)", "Herramientas de Potencia", "Potencia de impacto 41 Joules, tasa de remoción 13 toneladas/día, control de vibración.", new BigDecimal("75000"), new BigDecimal("350000"), 4.6700, -74.1200, "Bogotá Engativá", "DISPONIBLE", "https://images.unsplash.com/photo-1572981779307-38b8cabb2407?w=800&auto=format&fit=crop"));
        TOOLS.add(createTool(6L, "Compactadora Tipo Canguro Weber MT 68kg", "Compactación", "Fuerza centrífuga 16 kN, motor Honda GX100 4 tiempos, zapata de acero templado.", new BigDecimal("85000"), new BigDecimal("400000"), 4.5980, -74.0750, "Bogotá Centro", "DISPONIBLE", "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=800&auto=format&fit=crop"));

        // Alquiler inicial de prueba
        Map<String, Object> r = new HashMap<>();
        r.put("id", 101L);
        r.put("toolId", 1L);
        r.put("toolName", "Retroexcavadora CAT 420F2");
        r.put("clientId", 1L);
        r.put("clientName", "Cristian Muñoz");
        r.put("startDate", "2026-10-12");
        r.put("endDate", "2026-10-15");
        r.put("days", 3);
        r.put("dailyRate", new BigDecimal("450000"));
        r.put("depositAmount", new BigDecimal("1200000"));
        r.put("total", new BigDecimal("2806500"));
        r.put("status", "CONFIRMADA");
        r.put("hasInvoice", true);
        r.put("invoiceId", 1L);
        RENTALS.add(r);
    }

    @GetMapping("/tools/public")
    @Operation(summary = "Catálogo público de herramientas", description = "Retorna la lista de toda la maquinaria y herramientas disponibles")
    public ResponseEntity<List<Map<String, Object>>> getPublicTools() {
        return ResponseEntity.ok(TOOLS);
    }

    @GetMapping("/tools/nearby")
    @Operation(summary = "Búsqueda por cercanía y geolocalización", description = "Filtra herramientas según latitud, longitud y radio en kilómetros")
    public ResponseEntity<List<Map<String, Object>>> getToolsNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "20") double radiusKm) {

        List<Map<String, Object>> filtered = TOOLS.stream().filter(t -> {
            double tLat = ((Number) t.get("latitude")).doubleValue();
            double tLng = ((Number) t.get("longitude")).doubleValue();
            double dist = haversineKm(lat, lng, tLat, tLng);
            return dist <= radiusKm;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    @PostMapping("/rentals")
    @Operation(summary = "Crear solicitud de reserva de maquinaria", description = "Registra una nueva reserva de alquiler")
    public ResponseEntity<Map<String, Object>> createRental(@RequestBody Map<String, Object> rentalData) {
        long id = 100L + RENTALS.size() + 1;
        rentalData.put("id", id);
        rentalData.put("status", "PENDIENTE_PAGO");
        rentalData.put("createdAt", java.time.LocalDateTime.now().toString());
        RENTALS.add(rentalData);
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalData);
    }

    @GetMapping("/rentals")
    @Operation(summary = "Listar reservas activas", description = "Retorna todas las reservas para el usuario o proveedor")
    public ResponseEntity<List<Map<String, Object>>> getRentals() {
        return ResponseEntity.ok(RENTALS);
    }

    @PutMapping("/rentals/{id}/status")
    @Operation(summary = "Actualizar estado de reserva", description = "Permite al proveedor aprobar o rechazar una reserva")
    public ResponseEntity<Map<String, Object>> updateRentalStatus(@PathVariable Long id, @RequestBody Map<String, String> statusBody) {
        for (Map<String, Object> r : RENTALS) {
            if (id.equals(r.get("id"))) {
                r.put("status", statusBody.get("status"));
                return ResponseEntity.ok(r);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/rentals/{id}/return")
    @Operation(summary = "Registrar devolución e inspección de daños", description = "Registra el acta de entrega final del equipo y la liberación de garantía")
    public ResponseEntity<Map<String, Object>> returnRental(@PathVariable Long id, @RequestBody Map<String, Object> returnData) {
        for (Map<String, Object> r : RENTALS) {
            if (id.equals(r.get("id"))) {
                r.put("status", "DEVUELTO");
                r.put("returnNotes", returnData.get("notes"));
                r.put("damageDetected", returnData.get("damageDetected"));
                r.put("damageCost", returnData.get("damageCost"));
                return ResponseEntity.ok(r);
            }
        }
        return ResponseEntity.notFound().build();
    }

    private static Map<String, Object> createTool(Long id, String name, String category, String description,
                                                  BigDecimal rate, BigDecimal deposit, double lat, double lng,
                                                  String locationName, String status, String image) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("category", category);
        map.put("description", description);
        map.put("dailyRate", rate);
        map.put("depositAmount", deposit);
        map.put("latitude", lat);
        map.put("longitude", lng);
        map.put("locationName", locationName);
        map.put("status", status);
        map.put("imageUrl", image);
        map.put("providerId", 2L);
        map.put("providerName", "Equipos & Maquinaria Colombia SAS");
        return map;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
