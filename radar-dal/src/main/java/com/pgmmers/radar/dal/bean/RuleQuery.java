package com.pgmmers.radar.dal.bean;

import com.pgmmers.radar.dal.bean.PageQuery;

public class RuleQuery extends PageQuery {

    private Long activationId;
    private String name;
    private Integer status;

    public Long getActivationId() {
        return activationId;
    }

    public void setActivationId(Long activationId) {
        this.activationId = activationId;
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
