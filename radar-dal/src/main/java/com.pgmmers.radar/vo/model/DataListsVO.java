package com.pgmmers.radar.vo.model;
import java.io.Serializable;
import java.util.Date;

public class DataListsVO implements Serializable{

    static final long serialVersionUID = 76543424L;

    private Long id;


    private String name;


    private String label;


    private Long modelId;


    private String comment;


    private String listType;


    private Integer status;


    private Date createTime;


    private Date updateTime;


    /**
     * operate
     */
    private String opt;


    public String getComment() {
        return comment;
    }


    public void setComment(String comment) {
        this.comment = comment;
    }


    public String getListType() {
        return listType;
    }


    public void setListType(String listType) {
        this.listType = listType;
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }



    public Integer getStatus() {
        return status;
    }


    public void setStatus(Integer status) {
        this.status = status;
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


    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }


    public String getOpt() {
        return opt;
    }


    public void setOpt(String opt) {
        this.opt = opt;
    }
    
    
}
