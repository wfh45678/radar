package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.springframework.stereotype.Component;

/** 最大值。 */
@Component
public class MaxAggregateFunction extends AbstractMongoAggregateFunction {

    @Override
    public AggregateKind kind() {
        return AggregateKind.MAX;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        var match = new org.bson.Document("$match", filter(q));
        var group = new org.bson.Document("$group",
                new org.bson.Document("_id", null).append("max", new org.bson.Document("$max", "$" + q.getFunctionField())));
        var doc = mongoService.aggregate(collection(q), java.util.Arrays.asList(match, group)).first();
        return doc == null ? null : new java.math.BigDecimal(doc.get("max").toString());
    }
}
