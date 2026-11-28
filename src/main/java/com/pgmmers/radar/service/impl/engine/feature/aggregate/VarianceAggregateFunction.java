package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.bson.Document;
import org.springframework.stereotype.Component;

/** 方差（v5.2.1 起服务端聚合：$group $stdDevPop 平方，不再全量拉内存；非数值行被服务端忽略）。 */
@Component
public class VarianceAggregateFunction extends AbstractMongoAggregateFunction {

    @Override
    public AggregateKind kind() {
        return AggregateKind.VARIANCE;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        Document doc = stdDevPopRow(q);
        // 空数据集/字段无数值：$stdDevPop 为 null → 返回 0（沿袭 v4.1 空集语义）
        if (doc == null || doc.get("stdDevPop") == null) {
            return java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        double sd = ((Number) doc.get("stdDevPop")).doubleValue();
        return new java.math.BigDecimal(sd * sd).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
