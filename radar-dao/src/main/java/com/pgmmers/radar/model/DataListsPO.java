package com.pgmmers.radar.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "engine_data_lists")
public class DataListsPO {
    /**
     * 主键
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 列表名
     */
    @Column(name = "NAME")
    private String name;

    /**
     * 列表名中文描叙
     */
    @Column(name = "LABEL")
    private String label;

    /**
     * 模型ID
     */
    @Column(name = "MODEL_ID")
    private Long modelId;

    /**
     * 注释
     */
    @Column(name = "COMMENT")
    private String comment;

    /**
     * 列表类型
     */
    @Column(name = "LIST_TYPE")
    private String listType;

    /**
     * 状态
     */
    @Column(name = "STATUS")
    private Integer status;

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
     * 获取列表名
     *
     * @return NAME - 列表名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置列表名
     *
     * @param name 列表名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取列表名中文描叙
     *
     * @return LABEL - 列表名中文描叙
     */
    public String getLabel() {
        return label;
    }

    /**
     * 设置列表名中文描叙
     *
     * @param label 列表名中文描叙
     */
    public void setLabel(String label) {
        this.label = label;
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
     * 获取列表类型
     *
     * @return LIST_TYPE - 列表类型
     */
    public String getListType() {
        return listType;
    }

    /**
     * 设置列表类型
     *
     * @param listType 列表类型
     */
    public void setListType(String listType) {
        this.listType = listType;
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