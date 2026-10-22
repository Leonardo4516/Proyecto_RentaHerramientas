package com.rentatools.application.tool.usecase;

import com.rentatools.application.tool.dto.ToolResponse;
import com.rentatools.domain.tool.ToolRepository;
import com.rentatools.domain.tool.ToolStatus;

import java.math.BigDecimal;
import java.util.List;

public class SearchToolsUseCase {
    private final ToolRepository toolRepository;

    public SearchToolsUseCase(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    public List<ToolResponse> execute(String query, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, ToolStatus status) {
        String q = (query != null && !query.isBlank()) ? query.trim().toLowerCase() : null;

        return toolRepository.findAll().stream()
                .filter(t -> {
                    if (q != null) {
                        boolean matchName = t.getName() != null && t.getName().toLowerCase().contains(q);
                        boolean matchBrand = t.getBrand() != null && t.getBrand().toLowerCase().contains(q);
                        boolean matchModel = t.getModel() != null && t.getModel().toLowerCase().contains(q);
                        boolean matchCode = t.getCode() != null && t.getCode().toLowerCase().contains(q);
                        if (!matchName && !matchBrand && !matchModel && !matchCode) {
                            return false;
                        }
                    }
                    if (categoryId != null && !categoryId.equals(t.getCategoryId())) {
                        return false;
                    }
                    if (status != null && t.getStatus() != status) {
                        return false;
                    }
                    if (minPrice != null && t.getDailyPrice() != null && t.getDailyPrice().compareTo(minPrice) < 0) {
                        return false;
                    }
                    if (maxPrice != null && t.getDailyPrice() != null && t.getDailyPrice().compareTo(maxPrice) > 0) {
                        return false;
                    }
                    return true;
                })
                .map(ToolResponse::fromDomain)
                .toList();
    }
}
