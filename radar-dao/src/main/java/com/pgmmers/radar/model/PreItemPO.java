package com.pgmmers.radar.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "engine_pre_item")
public class PreItemPO {
    /**
     * ID
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
     * 来源字段
     */
    @Column(name = "SOURCE_FIELD")
    private String sourceField;

    @Column(name = "SOURCE_LABEL")
    private String sourceLabel;

    /**
     * 目标字段
     */
    @Column(name = "DEST_FIELD")
    private String destField;

    @Column(name = "LABEL")
    private String label;

    /**
     * 参数
     */
    @Column(name = "ARGS")
    private String args;

    /**
     * 转换插件
     */
    @Column(name = "PLUGIN")
    private String plugin;

    @Column(name = "CONFIG_JSON")
    private String configJson;

    /**
     * 请求方式
     */
    @Column(name = "REQ_TYPE")
    private String reqType;

    @Column(name = "STATUS")
    private Integer status;

    @Column(name = "CREATE_TIME")
    private Date createTime;

    @Column(name = "UPDATE_TIME")
    private Date updateTime;

    /**
     * 获取ID
     *
     * @return ID - ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置ID
     *
     * @param id ID
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
     * 获取来源字段
     *
     * @return SOURCE_FIELD - 来源字段
     */
    public String getSourceField() {
        return sourceField;
    }

    /**
     * 设置来源字段
     *
     * @param sourceField 来源字段
     */
    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    /**
     * @return SOURCE_LABEL
     */
    public String getSourceLabel() {
        return sourceLabel;
    }

    /**
     * @param sourceLabel
     */
    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    /**
     * 获取目标字段
     *
     * @return DEST_FIELD - 目标字段
     */
    public String getDestField() {
        return destField;
    }

    /**
     * 设置目标字段
     *
     * @param destField 目标字段
     */
    public void setDestField(String destField) {
        this.destField = destField;
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
     * 获取参数
     *
     * @return ARGS - 参数
     */
    public String getArgs() {
        return args;
    }

    /**
     * 设置参数
     *
     * @param args 参数
     */
    public void setArgs(String args) {
        this.args = args;
    }

    /**
     * 获取转换插件
     *
     * @return PLUGIN - 转换插件
     */
    public String getPlugin() {
        return plugin;
    }

    /**
     * 设置转换插件
     *
     * @param plugin 转换插件
     */
    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    /**
     * @return CONFIG_JSON
     */
    public String getConfigJson() {
        return configJson;
    }

    /**
     * @param configJson
     */
    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    /**
     * 获取请求方式
     *
     * @return REQ_TYPE - 请求方式
     */
    public String getReqType() {
        return reqType;
    }

    /**
     * 设置请求方式
     *
     * @param reqType 请求方式
     */
    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    /**
     * @return STATUS
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(Integer status) {
        this.status = status;
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