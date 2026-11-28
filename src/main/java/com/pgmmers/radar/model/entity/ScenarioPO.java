package com.pgmmers.radar.model.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("engine_scenario")
public class ScenarioPO {
    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("GUID")
    private String guid;

    /**
     * 模型名称
     */
    @TableField("SCENARIO_NAME")
    private String scenarioName;

    @TableField("LABEL")
    private String label;

    /**
     * 事件中标识实体的主键
     */
    @TableField("ENTITY_NAME")
    private String entityName;

    /**
     * 事件主键
     */
    @TableField("ENTRY_NAME")
    private String entryName;

    /**
     * 事件时间
     */
    @TableField("REFERENCE_DATE")
    private String referenceDate;

    @TableField("FIELD_VALIDATE")
    private Boolean fieldValidate;

    @TableField("CODE")
    private String code;

    /**
     * 1 标识模板
     */
    @TableField("TEMPLATE")
    private Boolean template;

    @TableField("DASHBOARD_URL")
    private String dashboardUrl;

    /**
     * 状态
     */
    @TableField("STATUS")
    private Integer status;

    @TableField("CREATE_TIME")
    private Date createTime;

    @TableField("UPDATE_TIME")
    private Date updateTime;

    /**
     * 获取主键
     *
     * @return ID - 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键
     *
     * @param id 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return GUID
     */
    public String getGuid() {
        return guid;
    }

    /**
     * @param guid
     */
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * 获取模型名称
     *
     * @return MODEL_NAME - 模型名称
     */
    public String getScenarioName() {
        return scenarioName;
    }

    /**
     * 设置模型名称
     *
     * @param scenarioName 模型名称
     */
    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    /**
     * @return LABEL
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 获取事件中标识实体的主键
     *
     * @return ENTITY_NAME - 事件中标识实体的主键
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * 设置事件中标识实体的主键
     *
     * @param entityName 事件中标识实体的主键
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * 获取事件主键
     *
     * @return ENTRY_NAME - 事件主键
     */
    public String getEntryName() {
        return entryName;
    }

    /**
     * 设置事件主键
     *
     * @param entryName 事件主键
     */
    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    /**
     * 获取事件时间
     *
     * @return REFERENCE_DATE - 事件时间
     */
    public String getReferenceDate() {
        return referenceDate;
    }

    /**
     * 设置事件时间
     *
     * @param referenceDate 事件时间
     */
    public void setReferenceDate(String referenceDate) {
        this.referenceDate = referenceDate;
    }

    /**
     * @return FIELD_VALIDATE
     */
    public Boolean getFieldValidate() {
        return fieldValidate;
    }

    /**
     * @param fieldValidate
     */
    public void setFieldValidate(Boolean fieldValidate) {
        this.fieldValidate = fieldValidate;
    }

    /**
     * @return CODE
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取1 标识模板
     *
     * @return TEMPLATE - 1 标识模板
     */
    public Boolean getTemplate() {
        return template;
    }

    /**
     * 设置1 标识模板
     *
     * @param template 1 标识模板
     */
    public void setTemplate(Boolean template) {
        this.template = template;
    }

    /**
     * @return DASHBOARD_URL
     */
    public String getDashboardUrl() {
        return dashboardUrl;
    }

    /**
     * @param dashboardUrl
     */
    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    /**
     * 获取状态
     *
     * @return STATUS - 状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status 状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * @return CREATE_TIME
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return UPDATE_TIME
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}