package com.pgmmers.radar.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "engine_abstraction")
public class AbstractionPO {
    /**
     * 主键
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * Abstraction 名称
     */
    @Column(name = "NAME")
    private String name;

    @Column(name = "LABEL")
    private String label;

    /**
     * MODEL ID
     */
    @Column(name = "MODEL_ID")
    private Long modelId;

    /**
     * 统计类型
     */
    @Column(name = "AGGREGATE_TYPE")
    private Integer aggregateType;

    @Column(name = "SEARCH_FIELD")
    private String searchField;

    /**
     * 时间段类型
     */
    @Column(name = "SEARCH_INTERVAL_TYPE")
    private Integer searchIntervalType;

    /**
     * 时间段具体值
     */
    @Column(name = "SEARCH_INTERVAL_VALUE")
    private Integer searchIntervalValue;

    @Column(name = "FUNCTION_FIELD")
    private String functionField;

    /**
     * 数据校验
     */
    @Column(name = "RULE_SCRIPT")
    private String ruleScript;

    @Column(name = "RULE_DEFINITION")
    private String ruleDefinition;

    /**
     * 状态
     */
    @Column(name = "STATUS")
    private Integer status;

    /**
     * 备注
     */
    @Column(name = "COMMENT")
    private String comment;

    @Column(name = "CREATE_TIME")
    private Date createTime;

    @Column(name = "UPDATE_TIME")
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
     * 获取Abstraction 名称
     *
     * @return NAME - Abstraction 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置Abstraction 名称
     *
     * @param name Abstraction 名称
     */
    public void setName(String name) {
        this.name = name;
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
     * 获取MODEL ID
     *
     * @return MODEL_ID - MODEL ID
     */
    public Long getModelId() {
        return modelId;
    }

    /**
     * 设置MODEL ID
     *
     * @param modelId MODEL ID
     */
    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    /**
     * 获取统计类型
     *
     * @return AGGREGATE_TYPE - 统计类型
     */
    public Integer getAggregateType() {
        return aggregateType;
    }

    /**
     * 设置统计类型
     *
     * @param aggregateType 统计类型
     */
    public void setAggregateType(Integer aggregateType) {
        this.aggregateType = aggregateType;
    }

    /**
     * @return SEARCH_FIELD
     */
    public String getSearchField() {
        return searchField;
    }

    /**
     * @param searchField
     */
    public void setSearchField(String searchField) {
        this.searchField = searchField;
    }

    /**
     * 获取时间段类型
     *
     * @return SEARCH_INTERVAL_TYPE - 时间段类型
     */
    public Integer getSearchIntervalType() {
        return searchIntervalType;
    }

    /**
     * 设置时间段类型
     *
     * @param searchIntervalType 时间段类型
     */
    public void setSearchIntervalType(Integer searchIntervalType) {
        this.searchIntervalType = searchIntervalType;
    }

    /**
     * 获取时间段具体值
     *
     * @return SEARCH_INTERVAL_VALUE - 时间段具体值
     */
    public Integer getSearchIntervalValue() {
        return searchIntervalValue;
    }

    /**
     * 设置时间段具体值
     *
     * @param searchIntervalValue 时间段具体值
     */
    public void setSearchIntervalValue(Integer searchIntervalValue) {
        this.searchIntervalValue = searchIntervalValue;
    }

    /**
     * @return FUNCTION_FIELD
     */
    public String getFunctionField() {
        return functionField;
    }

    /**
     * @param functionField
     */
    public void setFunctionField(String functionField) {
        this.functionField = functionField;
    }

    /**
     * 获取数据校验
     *
     * @return RULE_SCRIPT - 数据校验
     */
    public String getRuleScript() {
        return ruleScript;
    }

    /**
     * 设置数据校验
     *
     * @param ruleScript 数据校验
     */
    public void setRuleScript(String ruleScript) {
        this.ruleScript = ruleScript;
    }

    /**
     * @return RULE_DEFINITION
     */
    public String getRuleDefinition() {
        return ruleDefinition;
    }

    /**
     * @param ruleDefinition
     */
    public void setRuleDefinition(String ruleDefinition) {
        this.ruleDefinition = ruleDefinition;
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
     * 获取备注
     *
     * @return COMMENT - 备注
     */
    public String getComment() {
        return comment;
    }

    /**
     * 设置备注
     *
     * @param comment 备注
     */
    public void setComment(String comment) {
        this.comment = comment;
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