package com.pgmmers.radar.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "engine_rule_history")
public class RuleHistoryPO {
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
    @Column(name = "RULE_ID")
    private Long ruleId;

    @Column(name = "MERCHANT_CODE")
    private String merchantCode;

    /**
     * 规则名称
     */
    @Column(name = "LABEL")
    private String label;

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

    @Column(name = "RULE_DEFINITION")
    private String ruleDefinition;

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
     * @return RULE_ID - 模型ID
     */
    public Long getRuleId() {
        return ruleId;
    }

    /**
     * 设置模型ID
     *
     * @param ruleId 模型ID
     */
    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * @return MERCHANT_CODE
     */
    public String getMerchantCode() {
        return merchantCode;
    }

    /**
     * @param merchantCode
     */
    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
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