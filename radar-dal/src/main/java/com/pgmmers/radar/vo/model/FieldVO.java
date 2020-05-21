package com.pgmmers.radar.vo.model;
import java.io.Serializable;
import java.util.Date;

public class FieldVO implements Serializable{
    static final long serialVersionUID = 7643552321L;

    private Long id;

    private Long modelId;

    private String fieldName;
    
    private String label;

    private String fieldType;

    private Date createTime;

    private Date updateTime;

    private String validateType;

    private String validateArgs;
    
    private Boolean indexed;


    public String getValidateType() {
        return validateType;
    }

    public void setValidateType(String validateType) {
        this.validateType = validateType;
    }

    public String getValidateArgs() {
        return validateArgs;
    }

    public void setValidateArgs(String validateArgs) {
        this.validateArgs = validateArgs;
    }

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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getIndexed() {
        return indexed;
    }

    public void setIndexed(Boolean indexed) {
        this.indexed = indexed;
    }

}
