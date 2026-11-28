package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.springframework.stereotype.Component;

/** 偏离率：|本次事件值 - 均值| × 100 / 均值（复用平均实现）。 */
@Component
public class DeviationAggregateFunction extends AbstractMongoAggregateFunction {

    private final AverageAggregateFunction averageFunction;

    public DeviationAggregateFunction(AverageAggregateFunction averageFunction) {
        this.averageFunction = averageFunction;
    }

    @Override
    public AggregateKind kind() {
        return AggregateKind.DEVIATION;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        java.math.BigDecimal avg = (java.math.BigDecimal) averageFunction.aggregate(q);
        // v4.1 修复：空数据集/均值为零不再 NPE 或除零，返回 0
        if (avg == null || java.math.BigDecimal.ZERO.compareTo(avg) == 0) {
            return java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        java.math.BigDecimal deviationVal = q.getCurrentEventValue().subtract(avg).abs();
        return deviationVal.multiply(new java.math.BigDecimal(100)).divide(avg, 2, java.math.RoundingMode.HALF_UP);
    }
}
