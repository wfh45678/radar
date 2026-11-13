package com.pgmmers.radar.model.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("engine_data_list_meta")
public class DataListMetaPO {
    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 数据列表ID
     */
    @TableField("DATA_LIST_ID")
    private Long dataListId;

    /**
     * 字段名
     */
    @TableField("FIELD_NAME")
    private String fieldName;

    @TableField("LABEL")
    private String label;

    /**
     * 字段序号
     */
    @TableField("SEQ_NUM")
    private Integer seqNum;

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
     * 获取数据列表ID
     *
     * @return DATA_LIST_ID - 数据列表ID
     */
    public Long getDataListId() {
        return dataListId;
    }

    /**
     * 设置数据列表ID
     *
     * @param dataListId 数据列表ID
     */
    public void setDataListId(Long dataListId) {
        this.dataListId = dataListId;
    }

    /**
     * 获取字段名
     *
     * @return FIELD_NAME - 字段名
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * 设置字段名
     *
     * @param fieldName 字段名
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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
     * 获取字段序号
     *
     * @return SEQ_NUM - 字段序号
     */
    public Integer getSeqNum() {
        return seqNum;
    }

    /**
     * 设置字段序号
     *
     * @param seqNum 字段序号
     */
    public void setSeqNum(Integer seqNum) {
        this.seqNum = seqNum;
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