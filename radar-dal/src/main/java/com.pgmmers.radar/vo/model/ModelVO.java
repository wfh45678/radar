package com.pgmmers.radar.vo.model;
import java.io.Serializable;
import java.util.Date;

public class ModelVO implements Serializable{

    static final long serialVersionUID = 2340056231L;

    private Long id;


    private String guid;


    private String modelName;

    private String label;

    private String entityName;


    private String entryName;


    private String referenceDate;


    private Integer status;


    private Date createTime;


    private Date updateTime;
    
    private Boolean fieldValidate;

    private String code;

    private Boolean template;
    
    private String dashboardUrl;
    
    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getGuid() {
        return guid;
    }


    public void setGuid(String guid) {
        this.guid = guid;
    }


    public String getModelName() {
        return modelName;
    }


    public void setModelName(String modelName) {
        this.modelName = modelName;
    }


    public String getEntityName() {
        return entityName;
    }


    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }


    public String getEntryName() {
        return entryName;
    }


    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }


    public String getReferenceDate() {
        return referenceDate;
    }


    public void setReferenceDate(String referenceDate) {
        this.referenceDate = referenceDate;
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


    public Boolean getFieldValidate() {
        return fieldValidate;
    }


    public void setFieldValidate(Boolean fieldValidate) {
        this.fieldValidate = fieldValidate;
    }


    public String getCode() {
        return code;
    }


    public void setCode(String merchantCode) {
        this.code = merchantCode;
    }


    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }


    public Boolean getTemplate() {
        return template;
    }


    public void setTemplate(Boolean template) {
        this.template = template;
    }


    public String getDashboardUrl() {
        return dashboardUrl;
    }


    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }
    
    
}
