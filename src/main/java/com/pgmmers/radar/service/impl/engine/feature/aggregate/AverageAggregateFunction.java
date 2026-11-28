package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.springframework.stereotype.Component;

/** 平均（v4.1 修复空数据集 NPE；补 status 过滤）。 */
@Component
public class AverageAggregateFunction extends AbstractMongoAggregateFunction {

    @Override
    public AggregateKind kind() {
        return AggregateKind.AVERAGE;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        var match = new org.bson.Document("$match", filter(q));
        var group = new org.bson.Document("$group",
                new org.bson.Document("_id", null).append("avg", new org.bson.Document("$avg", "$" + q.getFunctionField())));
        var doc = mongoService.aggregate(collection(q), java.util.Arrays.asList(match, group)).first();
        // v4.1 修复：空数据集（doc 为 null 或 avg 为 null）不再 NPE，返回 0
        if (doc == null || doc.get("avg") == null) {
            return java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return new java.math.BigDecimal(doc.get("avg").toString()).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
