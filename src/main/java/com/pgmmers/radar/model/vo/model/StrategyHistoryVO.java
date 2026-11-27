package com.pgmmers.radar.model.vo.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.Date;

/**
 * 策略定义变更历史 VO（v4.0）：definition 以 JsonNode 形态返回给前端。
 */
public class StrategyHistoryVO implements Serializable {

    static final long serialVersionUID = 65434233L;

    private Long id;

    private Long strategyId;

    private String merchantCode;

    private String type;

    private JsonNode definition;

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

    public JsonNode getDefinition() {
        return definition;
    }

    public void setDefinition(JsonNode definition) {
        this.definition = definition;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
