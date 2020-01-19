package com.pgmmers.radar.model;

import javax.persistence.*;

@Table(name = "engine_model_conf_param")
public class ModelConfParamPO {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    @Column(name = "mold_id")
    private Long moldId;

    private String feed;

    private String expressions;

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return mold_id
     */
    public Long getMoldId() {
        return moldId;
    }

    /**
     * @param moldId
     */
    public void setMoldId(Long moldId) {
        this.moldId = moldId;
    }

    /**
     * @return feed
     */
    public String getFeed() {
        return feed;
    }

    /**
     * @param feed
     */
    public void setFeed(String feed) {
        this.feed = feed;
    }

    /**
     * @return expressions
     */
    public String getExpressions() {
        return expressions;
    }

    /**
     * @param expressions
     */
    public void setExpressions(String expressions) {
        this.expressions = expressions;
    }
}