package com.pgmmers.radar.model.query;

public class FeatureQuery extends PageQuery {

    private Long modelId;

    /** 特征类型：ENRICH/AGGREGATE/DERIVED/RATIO（可空） */
    private String type;

    private String label;

    private Integer status;

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
