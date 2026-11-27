package com.pgmmers.radar.model.query;

import com.pgmmers.radar.model.query.PageQuery;

public class RuleQuery extends PageQuery {

    private Long strategyId;
    private String name;
    private Integer status;

    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
