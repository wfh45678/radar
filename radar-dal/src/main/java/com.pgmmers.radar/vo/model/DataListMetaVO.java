package com.pgmmers.radar.vo.model;
import java.io.Serializable;
import java.util.Date;

public class DataListMetaVO implements Serializable{
    static final long serialVersionUID = 90392342L;

    private Long id;


    private Long dataListId;


    private String fieldName;
    
    private String label;

    private Integer seqNum;

    private Date createTime;


    private Date updateTime;


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


    public String getFieldName() {
        return fieldName;
    }


    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }



    public Integer getSeqNum() {
        return seqNum;
    }


    public void setSeqNum(Integer seqNum) {
        this.seqNum = seqNum;
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


    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }


    
}
