package com.pgmmers.radar.model.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("engine_strategy")
public class StrategyPO {
    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 名称（策略包 JSON 键保持 activationName，兼容 v3.x 包格式，见 docs/model-package.md）
     */
    @JsonProperty("activationName")
    @TableField("STRATEGY_NAME")
    private String strategyName;

    /**
     * 策略类型（v4.0）：RULE_SET/SCORECARD/DECISION_TREE，见 {@link com.pgmmers.radar.enums.StrategyType}
     */
    @TableField("TYPE")
    private String type;

    /**
     * 策略类型化定义：评分卡/决策树的 JSON 文本；规则集类型为空（规则明细存 engine_rule）
     */
    @TableField("DEFINITION")
    private String definition;

    @TableField("LABEL")
    private String label;

    /**
     * model id
     */
    @TableField("MODEL_ID")
    private Long modelId;

    /**
     * 注释
     */
    @TableField("COMMENT")
    private String comment;

    /**
     * 底部阀值
     */
    @TableField("BOTTOM")
    private Integer bottom;

    /**
     * 中间阀值
     */
    @TableField("MEDIAN")
    private Integer median;

    /**
     * 顶部阀值
     */
    @TableField("HIGH")
    private Integer high;

    /**
     * 状态
     */
    @TableField("STATUS")
    private Integer status;

    @TableField("RULE_ORDER")
    private String ruleOrder;

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
     * 获取名称
     *
     * @return STRATEGY_NAME - 名称
     */
    public String getStrategyName() {
        return strategyName;
    }

    /**
     * 设置名称
     *
     * @param strategyName 名称
     */
    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    /**
     * @return TYPE - 策略类型
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return DEFINITION - 策略类型化定义 JSON 文本
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param definition
     */
    public void setDefinition(String definition) {
        this.definition = definition;
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
     * 获取model id
     *
     * @return MODEL_ID - model id
     */
    public Long getModelId() {
        return modelId;
    }

    /**
     * 设置model id
     *
     * @param modelId model id
     */
    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    /**
     * 获取注释
     *
     * @return COMMENT - 注释
     */
    public String getComment() {
        return comment;
    }

    /**
     * 设置注释
     *
     * @param comment 注释
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 获取底部阀值
     *
     * @return BOTTOM - 底部阀值
     */
    public Integer getBottom() {
        return bottom;
    }

    /**
     * 设置底部阀值
     *
     * @param bottom 底部阀值
     */
    public void setBottom(Integer bottom) {
        this.bottom = bottom;
    }

    /**
     * 获取中间阀值
     *
     * @return MEDIAN - 中间阀值
     */
    public Integer getMedian() {
        return median;
    }

    /**
     * 设置中间阀值
     *
     * @param median 中间阀值
     */
    public void setMedian(Integer median) {
        this.median = median;
    }

    /**
     * 获取顶部阀值
     *
     * @return HIGH - 顶部阀值
     */
    public Integer getHigh() {
        return high;
    }

    /**
     * 设置顶部阀值
     *
     * @param high 顶部阀值
     */
    public void setHigh(Integer high) {
        this.high = high;
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
     * @return RULE_ORDER
     */
    public String getRuleOrder() {
        return ruleOrder;
    }

    /**
     * @param ruleOrder
     */
    public void setRuleOrder(String ruleOrder) {
        this.ruleOrder = ruleOrder;
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