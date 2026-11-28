package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.springframework.stereotype.Component;

/** 去重计数（v4.1 起补 status 过滤，与 COUNT 对齐）。 */
@Component
public class DistinctCountAggregateFunction extends AbstractMongoAggregateFunction {

    @Override
    public AggregateKind kind() {
        return AggregateKind.DISTINCT_COUNT;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        return mongoService.distinctCount(collection(q), filter(q), q.getFunctionField());
    }
}
