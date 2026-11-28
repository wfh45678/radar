package com.pgmmers.radar.model.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("engine_data_lists")
public class DataListsPO {
    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 列表名
     */
    @TableField("NAME")
    private String name;

    /**
     * 列表名中文描叙
     */
    @TableField("LABEL")
    private String label;

    /**
     * 模型ID
     */
    @TableField("SCENARIO_ID")
    private Long scenarioId;

    /**
     * 注释
     */
    @TableField("COMMENT")
    private String comment;

    /**
     * 列表类型
     */
    @TableField("LIST_TYPE")
    private String listType;

    /**
     * 状态
     */
    @TableField("STATUS")
    private Integer status;

    @TableField("CREATE_TIME")
    private Date createTime;

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
     * @return SCENARIO_ID - 模型ID
     */
    public Long getModelId() {
        return scenarioId;
    }

    /**
     * 设置模型ID
     *
     * @param scenarioId 模型ID
     */
    public void setModelId(Long scenarioId) {
        this.scenarioId = scenarioId;
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