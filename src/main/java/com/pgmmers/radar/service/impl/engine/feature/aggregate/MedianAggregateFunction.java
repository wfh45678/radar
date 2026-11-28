package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.springframework.stereotype.Component;

/** 中位数。 */
@Component
public class MedianAggregateFunction extends AbstractMongoAggregateFunction {

    @Override
    public AggregateKind kind() {
        return AggregateKind.MEDIAN;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        var match = new org.bson.Document("$match", filter(q));
        var sort = new org.bson.Document("$sort", new org.bson.Document(q.getFunctionField(), 1));
        java.util.List<org.bson.Document> rows = new java.util.ArrayList<>();
        mongoService.aggregate(collection(q), java.util.Arrays.asList(match, sort)).forEach(rows::add);
        if (rows.isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        java.math.BigDecimal median = new java.math.BigDecimal(
                rows.get(rows.size() / 2).get(q.getFunctionField()).toString());
        if (rows.size() % 2 == 0) {
            median = median.add(new java.math.BigDecimal(
                    rows.get(rows.size() / 2 - 1).get(q.getFunctionField()).toString()))
                    .divide(new java.math.BigDecimal(2), 2, java.math.RoundingMode.HALF_UP);
        }
        return median;
    }
}
