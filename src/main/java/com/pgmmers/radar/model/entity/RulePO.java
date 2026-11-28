package com.pgmmers.radar.model.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("engine_rule")
public class RulePO {
    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 模型ID
     */
    @TableField("SCENARIO_ID")
    private Long scenarioId;

    /**
     * 所属策略ID（策略包 JSON 键 v5.0 起正名 strategyId（docs/scenario-package.md））
     */
    @JsonProperty("strategyId")
    @TableField("STRATEGY_ID")
    private Long strategyId;

    @TableField("NAME")
    private String name;

    /**
     * 规则名称
     */
    @TableField("LABEL")
    private String label;

    /**
     * 检验脚本
     */
    @TableField("SCRIPTS")
    private String scripts;

    /**
     * 初始分数
     */
    @TableField("INIT_SCORE")
    private Integer initScore;

    /**
     * 基数
     */
    @TableField("BASE_NUM")
    private Integer baseNum;

    /**
     * 运算符
     */
    @TableField("OPERATOR")
    private String operator;

    /**
     * 特征名称
     */
    @TableField("FEATURE_NAME")
    private String featureName;

    /**
     * 比例
     */
    @TableField("RATE")
    private Integer rate;

    /**
     * 最大值
     */
    @TableField("MAX")
    private Integer max;

    /**
     * 状态
     */
    @TableField("STATUS")
    private Integer status;

    @TableField("RULE_DEFINITION")
    private String ruleDefinition;

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
     * 获取模型ID
     *
     * @return SCENARIO_ID - 模型ID
     */
    public Long getScenarioId() {
        return scenarioId;
    }

    /**
     * 设置模型ID
     *
     * @param scenarioId 模型ID
     */
    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    /**
     * 获取激活ID
     *
     * @return STRATEGY_ID - 激活ID
     */
    public Long getStrategyId() {
        return strategyId;
    }

    /**
     * 设置激活ID
     *
     * @param strategyId 激活ID
     */
    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    /**
     * @return NAME
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取规则名称
     *
     * @return LABEL - 规则名称
     */
    public String getLabel() {
        return label;
    }

    /**
     * 设置规则名称
     *
     * @param label 规则名称
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 获取检验脚本
     *
     * @return SCRIPTS - 检验脚本
     */
    public String getScripts() {
        return scripts;
    }

    /**
     * 设置检验脚本
     *
     * @param scripts 检验脚本
     */
    public void setScripts(String scripts) {
        this.scripts = scripts;
    }

    /**
     * 获取初始分数
     *
     * @return INIT_SCORE - 初始分数
     */
    public Integer getInitScore() {
        return initScore;
    }

    /**
     * 设置初始分数
     *
     * @param initScore 初始分数
     */
    public void setInitScore(Integer initScore) {
        this.initScore = initScore;
    }

    /**
     * 获取基数
     *
     * @return BASE_NUM - 基数
     */
    public Integer getBaseNum() {
        return baseNum;
    }

    /**
     * 设置基数
     *
     * @param baseNum 基数
     */
    public void setBaseNum(Integer baseNum) {
        this.baseNum = baseNum;
    }

    /**
     * 获取运算符
     *
     * @return OPERATOR - 运算符
     */
    public String getOperator() {
        return operator;
    }

    /**
     * 设置运算符
     *
     * @param operator 运算符
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * 获取特征名称
     *
     * @return FEATURE_NAME - 特征名称
     */
    public String getFeatureName() {
        return featureName;
    }

    /**
     * 设置特征名称
     *
     * @param featureName 特征名称
     */
    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    /**
     * 获取比例
     *
     * @return RATE - 比例
     */
    public Integer getRate() {
        return rate;
    }

    /**
     * 设置比例
     *
     * @param rate 比例
     */
    public void setRate(Integer rate) {
        this.rate = rate;
    }

    /**
     * 获取最大值
     *
     * @return MAX - 最大值
     */
    public Integer getMax() {
        return max;
    }

    /**
     * 设置最大值
     *
     * @param max 最大值
     */
    public void setMax(Integer max) {
        this.max = max;
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