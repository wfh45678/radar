package com.pgmmers.radar.service.engine.feature;

import com.pgmmers.radar.enums.FieldType;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 聚合查询参数（v4.1 特征工程）：由 AggregateExecutor 从特征 DEFINITION 解析构造，
 * 供 {@link AggregateFunction} 实现消费。时间条件统一作用于 refDateFieldName（毫秒 epoch）。
 */
public class AggregateQuery {

    private final String modelId;
    private final String searchField;
    private final Object searchFieldValue;
    private final String refDateFieldName;
    private final Date begin;
    private final Date end;
    private final String functionField;
    private final FieldType functionFieldType;
    /** 本次事件的被统计字段值（仅 DEVIATION 使用） */
    private final BigDecimal currentEventValue;

    private AggregateQuery(Builder builder) {
        this.modelId = builder.modelId;
        this.searchField = builder.searchField;
        this.searchFieldValue = builder.searchFieldValue;
        this.refDateFieldName = builder.refDateFieldName;
        this.begin = builder.begin;
        this.end = builder.end;
        this.functionField = builder.functionField;
        this.functionFieldType = builder.functionFieldType;
        this.currentEventValue = builder.currentEventValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getModelId() {
        return modelId;
    }

    public String getSearchField() {
        return searchField;
    }

    public Object getSearchFieldValue() {
        return searchFieldValue;
    }

    public String getRefDateFieldName() {
        return refDateFieldName;
    }

    public Date getBegin() {
        return begin;
    }

    public Date getEnd() {
        return end;
    }

    public String getFunctionField() {
        return functionField;
    }

    public FieldType getFunctionFieldType() {
        return functionFieldType;
    }

    public BigDecimal getCurrentEventValue() {
        return currentEventValue;
    }

    public static class Builder {
        private String modelId;
        private String searchField;
        private Object searchFieldValue;
        private String refDateFieldName;
        private Date begin;
        private Date end;
        private String functionField;
        private FieldType functionFieldType;
        private BigDecimal currentEventValue;

        public Builder modelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        public Builder searchField(String searchField) {
            this.searchField = searchField;
            return this;
        }

        public Builder searchFieldValue(Object value) {
            this.searchFieldValue = value;
            return this;
        }

        public Builder refDateFieldName(String name) {
            this.refDateFieldName = name;
            return this;
        }

        public Builder begin(Date begin) {
            this.begin = begin;
            return this;
        }

        public Builder end(Date end) {
            this.end = end;
            return this;
        }

        public Builder functionField(String functionField) {
            this.functionField = functionField;
            return this;
        }

        public Builder functionFieldType(FieldType type) {
            this.functionFieldType = type;
            return this;
        }

        public Builder currentEventValue(BigDecimal value) {
            this.currentEventValue = value;
            return this;
        }

        public AggregateQuery build() {
            return new AggregateQuery(this);
        }
    }
}
