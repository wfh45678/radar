package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** 中位数（v5.2.1 起精确取中位行：服务端 $sort + $skip/$limit 只取中间 1~2 行，内存 O(1)）。 */
@Component
public class MedianAggregateFunction extends AbstractMongoAggregateFunction {

    @Override
    public AggregateKind kind() {
        return AggregateKind.MEDIAN;
    }

    @Override
    public Object aggregate(AggregateQuery q) {
        String field = q.getFunctionField();
        long n = mongoService.count(collection(q), filter(q));
        if (n == 0) {
            return BigDecimal.ZERO;
        }
        var match = new Document("$match", filter(q));
        var sort = new Document("$sort", new Document(field, 1));
        List<Bson> pipeline = new ArrayList<>(List.of(match, sort));
        BigDecimal median;
        if (n % 2 == 1) {
            // 奇数：取正中 1 行（原值，不加 scale——沿袭旧实现语义）
            pipeline.add(new Document("$skip", (int) (n / 2)));
            pipeline.add(new Document("$limit", 1));
            Document doc = mongoService.aggregate(collection(q), pipeline).first();
            median = new BigDecimal(doc.get(field).toString());
        } else {
            // 偶数：取中间 2 行取均值（scale 2 HALF_UP——沿袭旧实现语义）
            pipeline.add(new Document("$skip", (int) (n / 2 - 1)));
            pipeline.add(new Document("$limit", 2));
            List<Document> rows = new ArrayList<>();
            mongoService.aggregate(collection(q), pipeline).forEach(rows::add);
            BigDecimal m1 = new BigDecimal(rows.get(0).get(field).toString());
            BigDecimal m2 = new BigDecimal(rows.get(1).get(field).toString());
            median = m1.add(m2).divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
        }
        return median;
    }
}
