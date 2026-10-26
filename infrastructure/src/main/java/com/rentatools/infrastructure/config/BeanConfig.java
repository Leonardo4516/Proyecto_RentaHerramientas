package com.rentatools.infrastructure.config;

import com.rentatools.application.rental.usecase.ChangeRentalStatusUseCase;
import com.rentatools.application.rental.usecase.CheckAvailabilityUseCase;
import com.rentatools.application.rental.usecase.CreateRentalBookingUseCase;
import com.rentatools.application.rental.usecase.ExtendRentalBookingUseCase;
import com.rentatools.application.tool.usecase.FindNearbyToolsUseCase;
import com.rentatools.application.tool.usecase.GetToolByIdUseCase;
import com.rentatools.application.tool.usecase.ListToolsUseCase;
import com.rentatools.application.tool.usecase.RegisterToolUseCase;
import com.rentatools.application.tool.usecase.SearchToolsUseCase;
import com.rentatools.application.tool.usecase.UpdateToolUseCase;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.tool.ToolRepository;
import com.rentatools.application.rental.usecase.GetReturnInspectionUseCase;
import com.rentatools.application.rental.usecase.RegisterReturnInspectionUseCase;
import com.rentatools.application.tool.usecase.CreateToolCategoryUseCase;
import com.rentatools.application.tool.usecase.UpdateToolCategoryUseCase;
import com.rentatools.domain.billing.port.PaymentRepository;
import com.rentatools.domain.notification.port.NotificationPort;
import com.rentatools.domain.tool.ToolCategoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public SearchToolsUseCase searchToolsUseCase(ToolRepository toolRepository) {
        return new SearchToolsUseCase(toolRepository);
    }

    @Bean
    public ListToolsUseCase listToolsUseCase(ToolRepository toolRepository) {
        return new ListToolsUseCase(toolRepository);
    }

    @Bean
    public GetToolByIdUseCase getToolByIdUseCase(ToolRepository toolRepository) {
        return new GetToolByIdUseCase(toolRepository);
    }

    @Bean
    public RegisterToolUseCase registerToolUseCase(ToolRepository toolRepository) {
        return new RegisterToolUseCase(toolRepository);
    }

    @Bean
    public UpdateToolUseCase updateToolUseCase(ToolRepository toolRepository) {
        return new UpdateToolUseCase(toolRepository);
    }

    @Bean
    public FindNearbyToolsUseCase findNearbyToolsUseCase(ToolRepository toolRepository) {
        return new FindNearbyToolsUseCase(toolRepository);
    }

    @Bean
    public CheckAvailabilityUseCase checkAvailabilityUseCase(RentalRepository rentalRepository) {
        return new CheckAvailabilityUseCase(rentalRepository);
    }

    @Bean
    public CreateRentalBookingUseCase createRentalBookingUseCase(RentalRepository rentalRepository, ToolRepository toolRepository) {
        return new CreateRentalBookingUseCase(rentalRepository, toolRepository);
    }

    @Bean
    public ExtendRentalBookingUseCase extendRentalBookingUseCase(RentalRepository rentalRepository, ToolRepository toolRepository) {
        return new ExtendRentalBookingUseCase(rentalRepository, toolRepository);
    }

    @Bean
    public ChangeRentalStatusUseCase changeRentalStatusUseCase(RentalRepository rentalRepository) {
        return new ChangeRentalStatusUseCase(rentalRepository);
    }

    @Bean
    public com.rentatools.application.rental.service.ReservaService reservaService(RentalRepository rentalRepository, ToolRepository toolRepository) {
        return new com.rentatools.application.rental.service.ReservaService(rentalRepository, toolRepository);
    }

    @Bean
    public RegisterReturnInspectionUseCase registerReturnInspectionUseCase(RentalRepository rentalRepository,
                                                                           ToolRepository toolRepository,
                                                                           PaymentRepository paymentRepository,
                                                                           NotificationPort notificationPort) {
        return new RegisterReturnInspectionUseCase(rentalRepository, toolRepository, paymentRepository, notificationPort);
    }

    @Bean
    public GetReturnInspectionUseCase getReturnInspectionUseCase(RentalRepository rentalRepository) {
        return new GetReturnInspectionUseCase(rentalRepository);
    }

    @Bean
    public CreateToolCategoryUseCase createToolCategoryUseCase(ToolCategoryRepository categoryRepository) {
        return new CreateToolCategoryUseCase(categoryRepository);
    }

    @Bean
    public UpdateToolCategoryUseCase updateToolCategoryUseCase(ToolCategoryRepository categoryRepository) {
        return new UpdateToolCategoryUseCase(categoryRepository);
    }

    @Bean
    public com.rentatools.application.tool.usecase.RegisterToolMaintenanceUseCase registerToolMaintenanceUseCase(
            com.rentatools.domain.tool.ToolMaintenanceRepository maintenanceRepository,
            ToolRepository toolRepository) {
        return new com.rentatools.application.tool.usecase.RegisterToolMaintenanceUseCase(maintenanceRepository, toolRepository);
    }

    @Bean
    public com.rentatools.application.tool.usecase.GetToolMaintenanceHistoryUseCase getToolMaintenanceHistoryUseCase(
            com.rentatools.domain.tool.ToolMaintenanceRepository maintenanceRepository) {
        return new com.rentatools.application.tool.usecase.GetToolMaintenanceHistoryUseCase(maintenanceRepository);
    }
}
