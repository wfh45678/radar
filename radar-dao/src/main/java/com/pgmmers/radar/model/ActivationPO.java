package com.pgmmers.radar.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "engine_activation")
public class ActivationPO {
    /**
     * 主键
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 名称
     */
    @Column(name = "ACTIVATION_NAME")
    private String activationName;

    @Column(name = "LABEL")
    private String label;

    /**
     * model id
     */
    @Column(name = "MODEL_ID")
    private Long modelId;

    /**
     * 注释
     */
    @Column(name = "COMMENT")
    private String comment;

    /**
     * 底部阀值
     */
    @Column(name = "BOTTOM")
    private Integer bottom;

    /**
     * 中间阀值
     */
    @Column(name = "MEDIAN")
    private Integer median;

    /**
     * 顶部阀值
     */
    @Column(name = "HIGH")
    private Integer high;

    /**
     * 状态
     */
    @Column(name = "STATUS")
    private Integer status;

    @Column(name = "RULE_ORDER")
    private String ruleOrder;

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
     * 获取名称
     *
     * @return ACTIVATION_NAME - 名称
     */
    public String getActivationName() {
        return activationName;
    }

    /**
     * 设置名称
     *
     * @param activationName 名称
     */
    public void setActivationName(String activationName) {
        this.activationName = activationName;
    }

    /**
     * @return LABEL
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 获取model id
     *
     * @return MODEL_ID - model id
     */
    public Long getModelId() {
        return modelId;
    }

    /**
     * 设置model id
     *
     * @param modelId model id
     */
    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    /**
     * 获取注释
     *
     * @return COMMENT - 注释
     */
    public String getComment() {
        return comment;
    }

    /**
     * 设置注释
     *
     * @param comment 注释
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 获取底部阀值
     *
     * @return BOTTOM - 底部阀值
     */
    public Integer getBottom() {
        return bottom;
    }

    /**
     * 设置底部阀值
     *
     * @param bottom 底部阀值
     */
    public void setBottom(Integer bottom) {
        this.bottom = bottom;
    }

    /**
     * 获取中间阀值
     *
     * @return MEDIAN - 中间阀值
     */
    public Integer getMedian() {
        return median;
    }

    /**
     * 设置中间阀值
     *
     * @param median 中间阀值
     */
    public void setMedian(Integer median) {
        this.median = median;
    }

    /**
     * 获取顶部阀值
     *
     * @return HIGH - 顶部阀值
     */
    public Integer getHigh() {
        return high;
    }

    /**
     * 设置顶部阀值
     *
     * @param high 顶部阀值
     */
    public void setHigh(Integer high) {
        this.high = high;
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
     * @return RULE_ORDER
     */
    public String getRuleOrder() {
        return ruleOrder;
    }

    /**
     * @param ruleOrder
     */
    public void setRuleOrder(String ruleOrder) {
        this.ruleOrder = ruleOrder;
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