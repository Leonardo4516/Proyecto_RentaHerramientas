package com.rentatools.application.tool;

import com.rentatools.application.tool.dto.CreateToolCategoryCommand;
import com.rentatools.application.tool.dto.ToolCategoryResponse;
import com.rentatools.application.tool.usecase.CreateToolCategoryUseCase;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.tool.ToolCategory;
import com.rentatools.domain.tool.ToolCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateToolCategoryUseCaseTest {

    @Mock
    private ToolCategoryRepository categoryRepository;

    private CreateToolCategoryUseCase createToolCategoryUseCase;

    @BeforeEach
    void setUp() {
        createToolCategoryUseCase = new CreateToolCategoryUseCase(categoryRepository);
    }

    @Test
    @DisplayName("Debe crear una nueva categoría de herramientas exitosamente")
    void shouldCreateCategorySuccessfully() {
        CreateToolCategoryCommand command = new CreateToolCategoryCommand(
                "Maquinaria Pesada",
                "Retroexcavadoras, motoniveladoras y compactadores",
                "fa-truck-monster",
                true
        );

        ToolCategory savedCategory = new ToolCategory(
                10L,
                "Maquinaria Pesada",
                "Retroexcavadoras, motoniveladoras y compactadores",
                "fa-truck-monster",
                true
        );

        when(categoryRepository.save(any(ToolCategory.class))).thenReturn(savedCategory);

        ToolCategoryResponse response = createToolCategoryUseCase.execute(command);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Maquinaria Pesada", response.name());
        assertEquals("fa-truck-monster", response.iconName());
        assertTrue(response.active());
        verify(categoryRepository, times(1)).save(any(ToolCategory.class));
    }

    @Test
    @DisplayName("Debe fallar si el nombre de categoría es nulo o vacío")
    void shouldThrowExceptionWhenNameIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new CreateToolCategoryCommand("", "Descripción", "icon", true));
    }
}
