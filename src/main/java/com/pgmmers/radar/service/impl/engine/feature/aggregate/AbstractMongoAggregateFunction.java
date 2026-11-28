package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.mongodb.client.model.Filters;
import com.pgmmers.radar.service.engine.feature.AggregateFunction;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import com.pgmmers.radar.service.data.MongoService;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 聚合函数基类：统一时间窗 + status='1' 过滤口径（v4.1 起，与 COUNT 对齐；
 * v3.x 仅 COUNT 过滤，行为变化见升级指南）。
 */
abstract class AbstractMongoAggregateFunction implements AggregateFunction {

    @Autowired
    protected MongoService mongoService;

    protected Bson filter(AggregateQuery q) {
        return Filters.and(
                Filters.eq(q.getSearchField(), q.getSearchFieldValue()),
                Filters.eq("status", "1"),
                Filters.gte(q.getRefDateFieldName(), q.getBegin().getTime()),
                Filters.lte(q.getRefDateFieldName(), q.getEnd().getTime()));
    }

    protected String collection(AggregateQuery q) {
        return "entity_" + q.getModelId();
    }

    /** SD 复用：方差实现（子类构造注入后赋值）。 */
    protected VarianceAggregateFunction varianceFunction;

    /** DEVIATION 复用：平均实现（子类构造注入后赋值）。 */
    protected AverageAggregateFunction averageFunction;
}
