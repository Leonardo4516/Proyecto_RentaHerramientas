package com.rentatools.infrastructure.persistence.entity;

import jakarta.persistence.*;
@Entity
@Table(name = "tool_specifications")
public class ToolSpecificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private ToolEntity tool;

    @Column(name = "spec_name", nullable = false, length = 100)
    private String specName;

    @Column(name = "spec_value", nullable = false, length = 255)
    private String specValue;

    public ToolSpecificationEntity() {}

    public ToolSpecificationEntity(Long id, ToolEntity tool, String specName, String specValue) {
        this.id = id;
        this.tool = tool;
        this.specName = specName;
        this.specValue = specValue;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ToolEntity getTool() { return tool; }
    public void setTool(ToolEntity tool) { this.tool = tool; }
    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public String getSpecValue() { return specValue; }
    public void setSpecValue(String specValue) { this.specValue = specValue; }
}
