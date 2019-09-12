package com.pgmmers.radar.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "engine_data_list_meta")
public class DataListMetaPO {
    /**
     * 主键
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 数据列表ID
     */
    @Column(name = "DATA_LIST_ID")
    private Long dataListId;

    /**
     * 字段名
     */
    @Column(name = "FIELD_NAME")
    private String fieldName;

    @Column(name = "LABEL")
    private String label;

    /**
     * 字段序号
     */
    @Column(name = "SEQ_NUM")
    private Integer seqNum;

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