package com.pgmmers.radar.dal.bean;

public class PreItemQuery extends PageQuery {

    private Long modelId;
    private String plugin;
    private Integer status;
    private String destField;
    private String label;

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDestField() {
        return destField;
    }

    public void setDestField(String destField) {
        this.destField = destField;
    }

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

    
}
