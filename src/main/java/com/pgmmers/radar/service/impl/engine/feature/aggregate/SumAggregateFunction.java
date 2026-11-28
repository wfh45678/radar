package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.springframework.stereotype.Component;

/** 求和。 */
@Component
public class SumAggregateFunction extends AbstractMongoAggregateFunction {

    @Override
    public AggregateKind kind() {
        return AggregateKind.SUM;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        var match = new org.bson.Document("$match", filter(q));
        var group = new org.bson.Document("$group",
                new org.bson.Document("_id", null).append("sum", new org.bson.Document("$sum", "$" + q.getFunctionField())));
        var doc = mongoService.aggregate(collection(q), java.util.Arrays.asList(match, group)).first();
        return doc == null ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(doc.get("sum").toString());
    }
}
