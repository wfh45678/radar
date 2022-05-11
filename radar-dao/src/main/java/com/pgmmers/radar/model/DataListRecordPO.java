package com.pgmmers.radar.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "engine_data_list_records")
public class DataListRecordPO {
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
     * 数据记录
     */
    @Column(name = "DATA_RECORD")
    private String dataRecord;

    /**
     * 数据描述
     */
    @Column(name = "DATA_REMARK")
    private String dataRemark;

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
     * 获取数据记录
     *
     * @return DATA_RECORD - 数据记录
     */
    public String getDataRecord() {
        return dataRecord;
    }

    /**
     * 设置数据记录
     *
     * @param dataRecord 数据记录
     */
    public void setDataRecord(String dataRecord) {
        this.dataRecord = dataRecord;
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

    public String getDataRemark() {
        return dataRemark;
    }

    public void setDataRemark(String dataRemark) {
        this.dataRemark = dataRemark;
    }
}
