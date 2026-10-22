package com.rentatools.infrastructure.config;

import com.rentatools.application.rental.usecase.ChangeRentalStatusUseCase;
import com.rentatools.application.rental.usecase.CheckAvailabilityUseCase;
import com.rentatools.application.rental.usecase.CreateRentalBookingUseCase;
import com.rentatools.application.tool.usecase.FindNearbyToolsUseCase;
import com.rentatools.application.tool.usecase.GetToolByIdUseCase;
import com.rentatools.application.tool.usecase.ListToolsUseCase;
import com.rentatools.application.tool.usecase.RegisterToolUseCase;
import com.rentatools.application.tool.usecase.SearchToolsUseCase;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.tool.ToolRepository;
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
    public ChangeRentalStatusUseCase changeRentalStatusUseCase(RentalRepository rentalRepository) {
        return new ChangeRentalStatusUseCase(rentalRepository);
    }
}
