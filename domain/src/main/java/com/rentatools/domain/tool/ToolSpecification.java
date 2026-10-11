package com.rentatools.domain.tool;

import java.util.UUID;

public class ToolSpecification {
    private final UUID id;
    private final String specName;
    private final String specValue;

    public ToolSpecification(UUID id, String specName, String specValue) {
        this.id = id != null ? id : UUID.randomUUID();
        this.specName = specName;
        this.specValue = specValue;
    }

    public UUID getId() { return id; }
    public String getSpecName() { return specName; }
    public String getSpecValue() { return specValue; }
}
