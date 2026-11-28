package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 标准差（v5.2.1 起服务端聚合：$group $stdDevPop 直取，全精度舍入，不再经舍入方差开方）。 */
@Component
public class SdAggregateFunction extends AbstractMongoAggregateFunction {

    @Override
    public AggregateKind kind() {
        return AggregateKind.SD;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        Document doc = stdDevPopRow(q);
        if (doc == null || doc.get("stdDevPop") == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(((Number) doc.get("stdDevPop")).doubleValue())
                .setScale(2, RoundingMode.HALF_UP);
    }
}
