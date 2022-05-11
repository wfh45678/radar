package com.pgmmers.radar.vo.model;
import java.io.Serializable;
import java.util.Date;

public class DataListRecordVO implements Serializable{

    static final long serialVersionUID = 455234234L;

    private Long id;


    private Long dataListId;


    private String dataRecord;

    private String dataRemark;

    private Date createTime;


    private Date updateTime;

    private Long modelId;

    /**
     * operate
     */
    private String opt;

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getDataListId() {
        return dataListId;
    }


    public void setDataListId(Long dataListId) {
        this.dataListId = dataListId;
    }


    public String getDataRecord() {
        return dataRecord;
    }


    public void setDataRecord(String dataRecord) {
        this.dataRecord = dataRecord;
    }


    public Date getCreateTime() {
        return createTime;
    }


    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    public Date getUpdateTime() {
        return updateTime;
    }


    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }


    public Long getModelId() {
        return modelId;
    }


    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }


    public String getOpt() {
        return opt;
    }


    public void setOpt(String opt) {
        this.opt = opt;
    }

    public String getDataRemark() {
        return dataRemark;
    }

    public void setDataRemark(String dataRemark) {
        this.dataRemark = dataRemark;
    }

    @Override
    public String toString() {
        return "DataListRecordVO{" +
                "id=" + id +
                ", dataListId=" + dataListId +
                ", dataRecord='" + dataRecord + '\'' +
                ", modelId=" + modelId +
                ", opt='" + opt + '\'' +
                '}';
    }
}
