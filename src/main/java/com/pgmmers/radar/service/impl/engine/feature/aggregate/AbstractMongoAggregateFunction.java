package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.mongodb.client.model.Filters;
import org.bson.Document;
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
        return "entity_" + q.getScenarioId();
    }

    /**
     * 服务端总体标准差（$group $stdDevPop，v5.2.1 起取代全量拉内存计算；
     * Mongo 3.2+ 支持，非数值行被服务端忽略）。
     *
     * @return 单行分组结果（stdDevPop 键），空数据集返回 null
     */
    protected Document stdDevPopRow(AggregateQuery q) {
        var match = new Document("$match", filter(q));
        var group = new Document("$group", new Document("_id", null)
                .append("stdDevPop", new Document("$stdDevPop", "$" + q.getFunctionField())));
        return mongoService.aggregate(collection(q), java.util.Arrays.asList(match, group)).first();
    }
}
