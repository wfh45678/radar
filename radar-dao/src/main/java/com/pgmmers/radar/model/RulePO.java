package com.pgmmers.radar.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "engine_rule")
public class RulePO {
    /**
     * 主键
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 模型ID
     */
    @Column(name = "MODEL_ID")
    private Long modelId;

    /**
     * 激活ID
     */
    @Column(name = "ACTIVATION_ID")
    private Long activationId;

    @Column(name = "NAME")
    private String name;

    /**
     * 规则名称
     */
    @Column(name = "LABEL")
    private String label;

    /**
     * 检验脚本
     */
    @Column(name = "SCRIPTS")
    private String scripts;

    /**
     * 初始分数
     */
    @Column(name = "INIT_SCORE")
    private Integer initScore;

    /**
     * 基数
     */
    @Column(name = "BASE_NUM")
    private Integer baseNum;

    /**
     * 运算符
     */
    @Column(name = "OPERATOR")
    private String operator;

    /**
     * 抽象名称
     */
    @Column(name = "ABSTRACTION_NAME")
    private String abstractionName;

    /**
     * 比例
     */
    @Column(name = "RATE")
    private Integer rate;

    /**
     * 最大值
     */
    @Column(name = "MAX")
    private Integer max;

    /**
     * 状态
     */
    @Column(name = "STATUS")
    private Integer status;

    @Column(name = "RULE_DEFINITION")
    private String ruleDefinition;

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
     * 获取模型ID
     *
     * @return MODEL_ID - 模型ID
     */
    public Long getModelId() {
        return modelId;
    }

    /**
     * 设置模型ID
     *
     * @param modelId 模型ID
     */
    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    /**
     * 获取激活ID
     *
     * @return ACTIVATION_ID - 激活ID
     */
    public Long getActivationId() {
        return activationId;
    }

    /**
     * 设置激活ID
     *
     * @param activationId 激活ID
     */
    public void setActivationId(Long activationId) {
        this.activationId = activationId;
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
     * 获取抽象名称
     *
     * @return ABSTRACTION_NAME - 抽象名称
     */
    public String getAbstractionName() {
        return abstractionName;
    }

    /**
     * 设置抽象名称
     *
     * @param abstractionName 抽象名称
     */
    public void setAbstractionName(String abstractionName) {
        this.abstractionName = abstractionName;
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