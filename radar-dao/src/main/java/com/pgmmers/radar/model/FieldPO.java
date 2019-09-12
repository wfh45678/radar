package com.pgmmers.radar.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "engine_field")
public class FieldPO {
    /**
     * 主键
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * MODEL ID
     */
    @Column(name = "MODEL_ID")
    private Long modelId;

    /**
     * 事件信息中的包含的字段
     */
    @Column(name = "FIELD_NAME")
    private String fieldName;

    @Column(name = "LABEL")
    private String label;

    /**
     * 字段类型
     */
    @Column(name = "FIELD_TYPE")
    private String fieldType;

    /**
     * 校验类型
     */
    @Column(name = "VALIDATE_TYPE")
    private String validateType;

    /**
     * 校验参数
     */
    @Column(name = "VALIDATE_ARGS")
    private String validateArgs;

    /**
     * 1 标识索引字段
     */
    @Column(name = "INDEXED")
    private Boolean indexed;

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
     * 获取MODEL ID
     *
     * @return MODEL_ID - MODEL ID
     */
    public Long getModelId() {
        return modelId;
    }

    /**
     * 设置MODEL ID
     *
     * @param modelId MODEL ID
     */
    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    /**
     * 获取事件信息中的包含的字段
     *
     * @return FIELD_NAME - 事件信息中的包含的字段
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * 设置事件信息中的包含的字段
     *
     * @param fieldName 事件信息中的包含的字段
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
     * 获取字段类型
     *
     * @return FIELD_TYPE - 字段类型
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * 设置字段类型
     *
     * @param fieldType 字段类型
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * 获取校验类型
     *
     * @return VALIDATE_TYPE - 校验类型
     */
    public String getValidateType() {
        return validateType;
    }

    /**
     * 设置校验类型
     *
     * @param validateType 校验类型
     */
    public void setValidateType(String validateType) {
        this.validateType = validateType;
    }

    /**
     * 获取校验参数
     *
     * @return VALIDATE_ARGS - 校验参数
     */
    public String getValidateArgs() {
        return validateArgs;
    }

    /**
     * 设置校验参数
     *
     * @param validateArgs 校验参数
     */
    public void setValidateArgs(String validateArgs) {
        this.validateArgs = validateArgs;
    }

    /**
     * 获取1 标识索引字段
     *
     * @return INDEXED - 1 标识索引字段
     */
    public Boolean getIndexed() {
        return indexed;
    }

    /**
     * 设置1 标识索引字段
     *
     * @param indexed 1 标识索引字段
     */
    public void setIndexed(Boolean indexed) {
        this.indexed = indexed;
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