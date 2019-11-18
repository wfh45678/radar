package com.pgmmers.radar.vo.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.Date;

public class PreItemVO implements Serializable{
    static final long serialVersionUID = 45420231756L;

    private Long id;


    private Long modelId;


    private String sourceField;
    
    private String sourceLabel;


    private String destField;

    private String label;

    private String plugin;

    private String reqType = "GET";

    @JsonProperty
    private JsonNode configJson;

    private Integer status;


    private Date createTime;


    private Date updateTime;

    private String args;

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getModelId() {
        return modelId;
    }


    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }


    public String getSourceField() {
        return sourceField;
    }


    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }


    public String getDestField() {
        return destField;
    }


    public void setDestField(String destField) {
        this.destField = destField;
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


    public Date getCreateTime() {
        return createTime;
    }


    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    public Date getUpdateTime() {
        return updateTime;
    }


    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }


    public String getArgs() {
        return args;
    }


    public void setArgs(String args) {
        this.args = args;
    }


    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }


	public String getSourceLabel() {
		return sourceLabel;
	}


	public void setSourceLabel(String sourceLabel) {
		this.sourceLabel = sourceLabel;
	}


    public JsonNode getConfigJson() {
        return configJson;
    }

    public void setConfigJson(JsonNode configJson) {
        this.configJson = configJson;
    }


    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }
}
