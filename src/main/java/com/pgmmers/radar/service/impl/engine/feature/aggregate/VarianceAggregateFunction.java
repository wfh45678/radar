package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.springframework.stereotype.Component;

/** 方差。 */
@Component
public class VarianceAggregateFunction extends AbstractMongoAggregateFunction {

    @Override
    public AggregateKind kind() {
        return AggregateKind.VARIANCE;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        java.util.List<java.math.BigDecimal> records = new java.util.ArrayList<>();
        mongoService.find(collection(q), filter(q)).forEach(t -> records.add(
                new java.math.BigDecimal(t.get(q.getFunctionField(), q.getFunctionFieldType().getClazz()).toString())));
        if (records.isEmpty()) {
            // v4.1 修复：空数据集除零（原实现会 ArithmeticException），返回 0
            return java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
        for (java.math.BigDecimal val : records) {
            sum = sum.add(val);
        }
        java.math.BigDecimal avg = sum.divide(new java.math.BigDecimal(records.size()), 2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal sq = java.math.BigDecimal.ZERO;
        for (java.math.BigDecimal val : records) {
            sq = sq.add(val.subtract(avg).multiply(val.subtract(avg)));
        }
        return sq.divide(new java.math.BigDecimal(records.size()), 2, java.math.RoundingMode.HALF_UP);
    }
}
