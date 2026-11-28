package com.pgmmers.radar.model.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("engine_rule_history")
public class RuleHistoryPO {
    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 模型ID
     */
    @TableField("RULE_ID")
    private Long ruleId;

    @TableField("USER_CODE")
    private String userCode;

    /**
     * 规则名称
     */
    @TableField("LABEL")
    private String label;

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

    @TableField("RULE_DEFINITION")
    private String ruleDefinition;

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
     * @return USER_CODE
     */
    public String getMerchantCode() {
        return userCode;
    }

    /**
     * @param userCode
     */
    public void setUserCode(String userCode) {
        this.userCode = userCode;
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