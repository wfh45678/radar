package com.pgmmers.radar.model.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 策略定义变更历史（v4.0）：评分卡/决策树每次保存快照 DEFINITION，
 * 规则集沿用 engine_rule_history。仅查询展示（与规则历史一致，暂不支持回滚）。
 */
@TableName("engine_strategy_history")
public class StrategyHistoryPO {
    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 策略ID
     */
    @TableField("STRATEGY_ID")
    private Long strategyId;

    /**
     * 操作人
     */
    @TableField("MERCHANT_CODE")
    private String merchantCode;

    /**
     * 策略类型
     */
    @TableField("TYPE")
    private String type;

    /**
     * 保存时的定义快照（JSON 字符串）
     */
    @JsonProperty("definition")
    @TableField("DEFINITION")
    private String definition;

    @TableField("UPDATE_TIME")
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
