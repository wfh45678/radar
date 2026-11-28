package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.springframework.stereotype.Component;

/** 标准差（复用方差实现）。 */
@Component
public class SdAggregateFunction extends AbstractMongoAggregateFunction {

    public SdAggregateFunction(VarianceAggregateFunction varianceFunction) {
        this.varianceFunction = varianceFunction;
    }

    @Override
    public AggregateKind kind() {
        return AggregateKind.SD;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        double variance = ((java.math.BigDecimal) varianceFunction.aggregate(q)).doubleValue();
        return new java.math.BigDecimal(Math.sqrt(variance)).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
