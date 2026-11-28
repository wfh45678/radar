package com.pgmmers.radar.service.impl.engine.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mongodb.client.model.Filters;
import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import com.pgmmers.radar.service.data.MongoService;
import java.util.Date;
import java.util.List;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;

/**
 * L1 纯单测：AggregateFunction 注册表（10 种聚合全注册 + 分发）与统一 status='1' 过滤口径。
 * MongoService 用 mock，只验证过滤器构造与分发行为，不连库。
 */
public class AggregateFunctionRegistryTest {

    private AggregateQuery query() {
        return com.pgmmers.radar.service.engine.feature.AggregateQuery.builder()
                .scenarioId("1").searchField("userId").searchFieldValue("u1")
                .refDateFieldName("radar_ref_datetime")
                .begin(new Date(0)).end(new Date())
                .functionField("amount")
                .functionFieldType(com.pgmmers.radar.enums.FieldType.DOUBLE)
                .currentEventValue(new java.math.BigDecimal("100"))
                .build();
    }

    @Test
    public void allTenKindsRegistered() {
        MongoService mongo = mock(MongoService.class);
        AggregateFunctionRegistry registry = new AggregateFunctionRegistry(List.of(
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.CountAggregateFunction(),
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.DistinctCountAggregateFunction(),
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.SumAggregateFunction(),
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.AverageAggregateFunction(),
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.MaxAggregateFunction(),
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.MinAggregateFunction(),
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.MedianAggregateFunction(),
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.VarianceAggregateFunction(),
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.SdAggregateFunction(),
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.DeviationAggregateFunction(
                        new com.pgmmers.radar.service.impl.engine.feature.aggregate.AverageAggregateFunction())));
        for (AggregateKind kind : AggregateKind.values()) {
            assertNotNull(registry.get(kind), "未注册: " + kind);
        }
        assertNull(registry.get(null));
    }

    @Test
    public void unifiedStatusFilterPresent() {
        // 口径统一（v4.1）：全部聚合过滤 status='1'（v3.x 仅 COUNT 过滤）
        MongoService mongo = mock(MongoService.class);
        when(mongo.count(anyString(), any(Bson.class))).thenReturn(1L);
        com.pgmmers.radar.service.impl.engine.feature.aggregate.CountAggregateFunction count =
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.CountAggregateFunction();
        ReflectionHolder.setMongo(count, mongo);
        count.aggregate(query());
        org.mockito.ArgumentCaptor<Bson> captor = org.mockito.ArgumentCaptor.forClass(Bson.class);
        org.mockito.Mockito.verify(mongo).count(anyString(), captor.capture());
        String rendered = captor.getValue().toString();
        org.junit.jupiter.api.Assertions.assertTrue(rendered.contains("status"),
                "过滤器应包含 status 条件: " + rendered);
        // DistinctCount 同口径
        when(mongo.distinctCount(anyString(), any(Bson.class), anyString())).thenReturn(1L);
        com.pgmmers.radar.service.impl.engine.feature.aggregate.DistinctCountAggregateFunction distinct =
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.DistinctCountAggregateFunction();
        ReflectionHolder.setMongo(distinct, mongo);
        distinct.aggregate(query());
        org.mockito.ArgumentCaptor<Bson> captor2 = org.mockito.ArgumentCaptor.forClass(Bson.class);
        org.mockito.Mockito.verify(mongo).distinctCount(anyString(), captor2.capture(), anyString());
        org.junit.jupiter.api.Assertions.assertTrue(captor2.getValue().toString().contains("status"),
                "DistinctCount 过滤器应包含 status 条件");
    }

    @Test
    public void averageEmptyDatasetReturnsZero() {
        // v4.1 修复：空数据集（pipeline 无结果行）不再 NPE
        MongoService mongo = mock(MongoService.class);
        com.mongodb.client.AggregateIterable<org.bson.Document> empty = stubAggregateIterable(null);
        when(mongo.aggregate(anyString(), any(List.class))).thenReturn(empty);
        com.pgmmers.radar.service.impl.engine.feature.aggregate.AverageAggregateFunction average =
                new com.pgmmers.radar.service.impl.engine.feature.aggregate.AverageAggregateFunction();
        ReflectionHolder.setMongo(average, mongo);
        Object result = average.aggregate(query());
        assertEquals(java.math.BigDecimal.ZERO.setScale(2), ((java.math.BigDecimal) result).setScale(2));
    }

    private com.mongodb.client.AggregateIterable<org.bson.Document> stubAggregateIterable(org.bson.Document doc) {
        com.mongodb.client.AggregateIterable<org.bson.Document> it = mock(com.mongodb.client.AggregateIterable.class);
        org.mockito.Mockito.when(it.first()).thenReturn(doc);
        return it;
    }

    /** 测试辅助：向基类 protected 字段注入 mock MongoService。 */
    static final class ReflectionHolder {
        static void setMongo(Object function, MongoService mongo) {
            org.springframework.test.util.ReflectionTestUtils.setField(function, "mongoService", mongo);
        }

        private ReflectionHolder() {
        }
    }
}
