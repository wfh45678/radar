package com.pgmmers.radar.model;

import javax.persistence.Table;

@Table(name = "engine_mold_param")
public class MoldParamPO {
    private Long id;
    private Long moldId;
    /**
     * 参数的key
     */
    private String feed;
    /**
     * 取数表达式，英文逗号分隔fields.deviceId，abstractions.log_uid_ip_1_day_qty
     */
    private String expressions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMoldId() {
        return moldId;
    }

    public void setMoldId(Long moldId) {
        this.moldId = moldId;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public String getExpressions() {
        return expressions;
    }

    public void setExpressions(String expressions) {
        this.expressions = expressions;
    }
}
