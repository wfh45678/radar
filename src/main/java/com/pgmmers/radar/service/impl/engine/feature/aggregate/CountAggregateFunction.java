package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.springframework.stereotype.Component;

/** 计数。 */
@Component
public class CountAggregateFunction extends AbstractMongoAggregateFunction {

    @Override
    public AggregateKind kind() {
        return AggregateKind.COUNT;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        return mongoService.count(collection(q), filter(q));
    }
}
