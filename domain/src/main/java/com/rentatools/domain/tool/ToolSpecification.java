package com.rentatools.domain.tool;

public class ToolSpecification {
    private final Long id;
    private final String specName;
    private final String specValue;

    public ToolSpecification(Long id, String specName, String specValue) {
        this.id = id != null ? id : null;
        this.specName = specName;
        this.specValue = specValue;
    }

    public Long getId() { return id; }
    public String getSpecName() { return specName; }
    public String getSpecValue() { return specValue; }
}
