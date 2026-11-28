package com.pgmmers.radar.model.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 特征（v4.1 特征工程重构）：预处理（PreItem）与抽象（Abstraction）的统一载体。
 * 类型化定义存 DEFINITION JSON（结构随 TYPE：ENRICH/AGGREGATE/DERIVED/RATIO），
 * 多列输出声明存 OUTPUT（[{column,title,type}]）。
 */
@TableName("engine_feature")
public class FeaturePO {
    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 特征名（上下文引用键 features.<featureName>；策略包 JSON 键保持 featureName）
     */
    @JsonProperty("featureName")
    @TableField("FEATURE_NAME")
    private String featureName;

    /**
     * 特征类型：ENRICH/AGGREGATE/DERIVED/RATIO
     */
    @JsonProperty("type")
    @TableField("TYPE")
    private String type;

    @TableField("LABEL")
    private String label;

    /**
     * model id
     */
    @TableField("SCENARIO_ID")
    private Long scenarioId;

    /**
     * 阶段内执行顺序（ENRICH/DERIVED 链式依赖按此排序）
     */
    @TableField("ORDER_NO")
    private Integer orderNo;

    /**
     * 备注
     */
    @TableField("COMMENT")
    private String comment;

    /**
     * 状态
     */
    @TableField("STATUS")
    private Integer status;

    /**
     * 类型化定义 JSON（ENRICH: plugin/args/sourceFields；AGGREGATE: aggregateType/searchField/
     * searchIntervalType/searchIntervalValue/functionField/filter；DERIVED: expression AST；RATIO: 组合定义）
     */
    @JsonProperty("definition")
    @TableField("DEFINITION")
    private String definition;

    /**
     * 多列输出列声明 [{column,title,type}]（ENRICH 多列插件；前端变量树展开子列）
     */
    @JsonProperty("output")
    @TableField("OUTPUT")
    private String output;

    @TableField("CREATE_TIME")
    private Date createTime;

    @TableField("UPDATE_TIME")
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
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

    public Long getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
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
}
