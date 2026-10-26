package com.rentatools.application.rental;

import com.rentatools.application.rental.dto.CreateRentalCommand;
import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.application.rental.usecase.CreateRentalBookingUseCase;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.rental.DateRange;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.tool.Tool;
import com.rentatools.domain.tool.ToolRepository;
import com.rentatools.domain.tool.ToolStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class CreateRentalBookingConcurrencyTest {

    private InMemoryRentalRepository rentalRepository;
    private InMemoryToolRepository toolRepository;
    private CreateRentalBookingUseCase useCase;

    @BeforeEach
    void setUp() {
        rentalRepository = new InMemoryRentalRepository();
        toolRepository = new InMemoryToolRepository();
        useCase = new CreateRentalBookingUseCase(rentalRepository, toolRepository);

        Tool tool = new Tool(
                100L,
                1L,
                1L,
                "TOOL-CONCUR-01",
                "Generador Diésel Concurrente 50kVA",
                "CAT",
                "XQ50",
                BigDecimal.valueOf(150000),
                BigDecimal.valueOf(500000),
                ToolStatus.DISPONIBLE,
                0,
                null,
                null,
                List.of(),
                List.of()
        );

        toolRepository.save(tool);
    }

    @Test
    @DisplayName("Concurrencia: Dos reservas simultáneas para la misma herramienta y fechas en competencia deben resultar en 1 éxito y 1 rechazo")
    void testConcurrentBookingsForSameToolAndDates() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyGate = new CountDownLatch(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threadCount);

        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(5);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<Throwable> unexpectedErrors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final long clientId = (i + 1) * 10L;
            executor.submit(() -> {
                readyGate.countDown();
                try {
                    startGate.await();
                    CreateRentalCommand command = new CreateRentalCommand(
                            clientId,
                            100L,
                            start,
                            end,
                            "Calle 100 # 15-20",
                            "Reserva concurrente"
                    );
                    RentalResponse response = useCase.execute(command);
                    if (response != null && response.id() != null) {
                        successCount.incrementAndGet();
                    }
                } catch (BusinessRuleValidationException ex) {
                    if (ex.getMessage().contains("No es posible reservar")) {
                        conflictCount.incrementAndGet();
                    } else {
                        unexpectedErrors.add(ex);
                    }
                } catch (Exception ex) {
                    unexpectedErrors.add(ex);
                } finally {
                    toolRepository.releaseLock();
                    doneGate.countDown();
                }
            });
        }

        assertTrue(readyGate.await(5, TimeUnit.SECONDS), "Los hilos deberían estar listos.");
        startGate.countDown(); // Liberar ambos hilos al mismo instante
        assertTrue(doneGate.await(5, TimeUnit.SECONDS), "Ambos hilos deberían completar ejecución.");
        executor.shutdown();

        assertTrue(unexpectedErrors.isEmpty(), "No debería haber errores inesperados: " + unexpectedErrors);
        assertEquals(1, successCount.get(), "Exactamente una reserva debe ser confirmada.");
        assertEquals(1, conflictCount.get(), "La otra solicitud en conflicto debe ser rechazada con BusinessRuleValidationException.");
        assertEquals(1, rentalRepository.findAll().size(), "En el repositorio debe quedar almacenada exactamente 1 reserva.");
    }

    @Test
    @DisplayName("Reservas consecutivas sin solapamiento de fechas deben completarse exitosamente")
    void testSequentialDisjointBookingsShouldSucceed() {
        LocalDate start1 = LocalDate.now().plusDays(1);
        LocalDate end1 = LocalDate.now().plusDays(3);

        LocalDate start2 = LocalDate.now().plusDays(4);
        LocalDate end2 = LocalDate.now().plusDays(7);

        CreateRentalCommand cmd1 = new CreateRentalCommand(10L, 100L, start1, end1, "Sitio A", "Reserva 1");
        CreateRentalCommand cmd2 = new CreateRentalCommand(20L, 100L, start2, end2, "Sitio B", "Reserva 2");

        RentalResponse resp1;
        try {
            resp1 = useCase.execute(cmd1);
        } finally {
            toolRepository.releaseLock();
        }

        RentalResponse resp2;
        try {
            resp2 = useCase.execute(cmd2);
        } finally {
            toolRepository.releaseLock();
        }

        assertNotNull(resp1.id());
        assertNotNull(resp2.id());
        assertEquals(2, rentalRepository.findAll().size());
    }

    // Repositorio en memoria simulando aislamiento y bloqueo de base de datos
    static class InMemoryRentalRepository implements RentalRepository {
        private final List<RentalBooking> store = new CopyOnWriteArrayList<>();
        private final ReentrantLock lock = new ReentrantLock();
        private long idGen = 1L;

        @Override
        public RentalBooking save(RentalBooking rental) {
            lock.lock();
            try {
                RentalBooking toSave = new RentalBooking(
                        rental.getId() != null ? rental.getId() : idGen++,
                        rental.getClientId(),
                        rental.getToolId(),
                        rental.getDateRange(),
                        rental.getDailyRate(),
                        rental.getDepositAmount(),
                        rental.getSubtotal(),
                        rental.getStatus(),
                        rental.getDeliveryAddress(),
                        rental.getNotes(),
                        rental.getCreatedAt()
                );
                store.add(toSave);
                return toSave;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Optional<RentalBooking> findById(Long id) {
            return store.stream().filter(r -> r.getId().equals(id)).findFirst();
        }

        @Override
        public List<RentalBooking> findByClientId(Long clientId) {
            return store.stream().filter(r -> r.getClientId().equals(clientId)).toList();
        }

        @Override
        public List<RentalBooking> findByToolId(Long toolId) {
            return store.stream().filter(r -> r.getToolId().equals(toolId)).toList();
        }

        @Override
        public List<RentalBooking> findActiveRentalsByToolId(Long toolId) {
            return store.stream().filter(r -> r.getToolId().equals(toolId)).toList();
        }

        @Override
        public List<RentalBooking> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public boolean hasOverlappingBookings(Long toolId, LocalDate startDate, LocalDate endDate) {
            lock.lock();
            try {
                DateRange requested = new DateRange(startDate, endDate);
                return store.stream()
                        .filter(r -> r.getToolId().equals(toolId))
                        .anyMatch(r -> r.getDateRange().overlapsWith(requested));
            } finally {
                lock.unlock();
            }
        }
    }

    static class InMemoryToolRepository implements ToolRepository {
        private final Map<Long, Tool> tools = new ConcurrentHashMap<>();
        private final ReentrantLock lock = new ReentrantLock();

        @Override
        public Tool save(Tool tool) {
            tools.put(tool.getId(), tool);
            return tool;
        }

        @Override
        public Optional<Tool> findById(Long id) {
            return Optional.ofNullable(tools.get(id));
        }

        @Override
        public Optional<Tool> findByIdWithLock(Long id) {
            lock.lock();
            // Simula adquisición de bloqueo pesimista en base de datos (SELECT ... FOR UPDATE)
            return Optional.ofNullable(tools.get(id));
        }

        public void releaseLock() {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        @Override
        public Optional<Tool> findByCode(String code) {
            return tools.values().stream().filter(t -> t.getCode().equals(code)).findFirst();
        }

        @Override
        public List<Tool> findAll() {
            return new ArrayList<>(tools.values());
        }

        @Override
        public List<Tool> findByCategoryId(Long categoryId) {
            return tools.values().stream().filter(t -> t.getCategoryId().equals(categoryId)).toList();
        }

        @Override
        public List<Tool> findByProviderId(Long providerId) {
            return tools.values().stream().filter(t -> t.getProviderId().equals(providerId)).toList();
        }

        @Override
        public List<Tool> findByStatus(ToolStatus status) {
            return tools.values().stream().filter(t -> t.getStatus().equals(status)).toList();
        }
    }
}
