package com.pgmmers.radar.service.impl.engine.feature.aggregate;

import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.service.data.MongoService;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * L1 纯单测（v5.2.1 统计聚合内存优化）：
 * MEDIAN 服务端 sort+skip/limit 只取中位 1~2 行（不再全量拉回）；
 * VARIANCE/SD 服务端 $stdDevPop 聚合（不再 find 全量）。
 * MongoService 用 mock，验证 pipeline 构造与计算语义（奇/偶/空集），不连库。
 */
public class MedianVarianceSdPipelineTest {

    private MongoService mongo;
    private MedianAggregateFunction median;
    private VarianceAggregateFunction variance;
    private SdAggregateFunction sd;

    @BeforeEach
    void setUp() {
        mongo = mock(MongoService.class);
        median = new MedianAggregateFunction();
        variance = new VarianceAggregateFunction();
        sd = new SdAggregateFunction();
        ReflectionTestUtils.setField(median, "mongoService", mongo);
        ReflectionTestUtils.setField(variance, "mongoService", mongo);
        ReflectionTestUtils.setField(sd, "mongoService", mongo);
    }

    private AggregateQuery query() {
        return AggregateQuery.builder()
                .scenarioId("1").searchField("userId").searchFieldValue("u1")
                .refDateFieldName("radar_ref_datetime")
                .begin(new Date(0)).end(new Date())
                .functionField("amount")
                .functionFieldType(FieldType.DOUBLE)
                .currentEventValue(new java.math.BigDecimal("100"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<Bson> capturedPipeline(MongoService mongo) {
        ArgumentCaptor<List<Bson>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(mongo).aggregate(anyString(), captor.capture());
        return captor.getValue();
    }

    /**
     * stub 抽象方法 iterator()（default 方法 first()/forEach 的默认实现会自然走 iterator——
     * 不直接 stub default 方法，规避 mockmaker 对 default 方法拦截不可靠的问题）。
     */
    @SuppressWarnings("unchecked")
    private com.mongodb.client.AggregateIterable<Document> stubIterable(Document... docs) {
        com.mongodb.client.MongoCursor<Document> cursor = mock(com.mongodb.client.MongoCursor.class);
        Boolean[] hasNext = new Boolean[docs.length + 1];
        Object[] next = new Object[docs.length];
        for (int i = 0; i < docs.length; i++) {
            hasNext[i] = true;
            next[i] = docs[i];
        }
        hasNext[docs.length] = false;
        Mockito.when(cursor.hasNext()).thenReturn(hasNext[0], java.util.Arrays.copyOfRange(hasNext, 1, hasNext.length));
        Mockito.when(cursor.next()).thenReturn((Document) next[0],
                java.util.Arrays.stream(next, 1, next.length).map(o -> (Document) o).toArray(Document[]::new));
        com.mongodb.client.AggregateIterable<Document> it = mock(com.mongodb.client.AggregateIterable.class);
        Mockito.doReturn(cursor).when(it).iterator();
        // 内联 mockmaker 会拦截 mock 的一切方法（含 default），不 stub 则 first()/forEach 返回 null/空
        Mockito.doAnswer(inv -> {
            // 兼容两种 forEach 重载：MongoIterable.forEach(Block) 与 Iterable.forEach(Consumer)
            Object block = inv.getArgument(0);
            for (Document d : docs) {
                if (block instanceof java.util.function.Consumer) {
                    ((java.util.function.Consumer<Document>) block).accept(d);
                } else {
                    ((com.mongodb.Block<Document>) block).apply(d);
                }
            }
            return null;
        }).when(it).forEach(Mockito.any());
        Mockito.doReturn(docs.length > 0 ? docs[0] : null).when(it).first();
        return it;
    }

    private void stubCount(long n) {
        when(mongo.count(anyString(), any(Bson.class))).thenReturn(n);
    }

    @Test
    @DisplayName("MEDIAN 奇数行：$skip/$limit 只取正中 1 行，原值不缩放")
    void medianOddRows() {
        stubCount(5);
        Document mid = new Document("amount", 30);
        var it = stubIterable(mid);
        when(mongo.aggregate(anyString(), any(List.class))).thenReturn(it);

        BigDecimal result = (BigDecimal) median.aggregate(query());

        assertEquals(new BigDecimal("30"), result);
        List<Bson> pipeline = capturedPipeline(mongo);
        // $match + $sort + $skip(2) + $limit(1)：只取正中 1 行
        assertEquals(4, pipeline.size());
        assertEquals(2, ((Document) pipeline.get(2)).get("$skip"));
        assertEquals(1, ((Document) pipeline.get(3)).get("$limit"));
    }

    @Test
    @DisplayName("MEDIAN 偶数行：取中间 2 行均值（scale 2）")
    void medianEvenRows() {
        stubCount(4);
        Document m1 = new Document("amount", 20);
        Document m2 = new Document("amount", 40);
        var it = stubIterable(m1, m2);
        when(mongo.aggregate(anyString(), any(List.class))).thenReturn(it);

        BigDecimal result = (BigDecimal) median.aggregate(query());

        assertEquals(0, new BigDecimal("30.00").compareTo(result));
        List<Bson> pipeline = capturedPipeline(mongo);
        assertEquals(1, ((Document) pipeline.get(2)).get("$skip"));
        assertEquals(2, ((Document) pipeline.get(3)).get("$limit"));
    }

    @Test
    @DisplayName("MEDIAN 空集：count=0 直接返回 0，不发起聚合")
    void medianEmpty() {
        stubCount(0);
        BigDecimal result = (BigDecimal) median.aggregate(query());
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
        verify(mongo, never()).aggregate(anyString(), any(List.class));
    }

    @Test
    @DisplayName("MEDIAN count 过滤器：统一 status='1' 口径")
    void medianCountFilterHasStatus() {
        stubCount(3);
        Document mid = new Document("amount", 30);
        var it = stubIterable(mid);
        when(mongo.aggregate(anyString(), any(List.class))).thenReturn(it);
        median.aggregate(query());

        ArgumentCaptor<Bson> captor = ArgumentCaptor.forClass(Bson.class);
        verify(mongo).count(anyString(), captor.capture());
        assertTrue(captor.getValue().toString().contains("status"),
                "count 过滤器应包含 status 条件: " + captor.getValue());
    }

    @Test
    @DisplayName("VARIANCE：$stdDevPop 服务端聚合，方差=sd²")
    void varianceServerSide() {
        Document row = new Document("stdDevPop", 10.0);
        var it = stubIterable(row);
        when(mongo.aggregate(anyString(), any(List.class))).thenReturn(it);

        BigDecimal result = (BigDecimal) variance.aggregate(query());
        assertEquals(0, new BigDecimal("100.00").compareTo(result));

        List<Bson> pipeline = capturedPipeline(mongo);
        String groupJson = ((Document) pipeline.get(1)).get("$group").toString();
        assertTrue(groupJson.contains("stdDevPop"), "$group 应含 $stdDevPop: " + groupJson);
    }

    @Test
    @DisplayName("SD：$stdDevPop 直取（全精度舍入）")
    void sdServerSide() {
        Document row = new Document("stdDevPop", 10.0);
        var it = stubIterable(row);
        when(mongo.aggregate(anyString(), any(List.class))).thenReturn(it);

        BigDecimal result = (BigDecimal) sd.aggregate(query());
        assertEquals(0, new BigDecimal("10.00").compareTo(result));
    }

    @Test
    @DisplayName("空集：VARIANCE/SD 返回 0.00（$stdDevPop 为 null）")
    void emptyDataset() {
        Document nullRow = new Document("stdDevPop", null);
        var it = stubIterable(nullRow);
        when(mongo.aggregate(anyString(), any(List.class))).thenReturn(it);

        BigDecimal vr = (BigDecimal) variance.aggregate(query());
        BigDecimal sr = (BigDecimal) sd.aggregate(query());
        assertEquals(0, new BigDecimal("0.00").compareTo(vr));
        assertEquals(0, new BigDecimal("0.00").compareTo(sr));
    }

    @Test
    @DisplayName("MEDIAN/VARIANCE 不再 find 全量拉回")
    void noFullScanFind() {
        Document m1 = new Document("amount", 20);
        Document m2 = new Document("amount", 40);
        stubCount(4);
        var medianIt = stubIterable(m1, m2);
        when(mongo.aggregate(anyString(), any(List.class))).thenReturn(medianIt);
        median.aggregate(query());

        Document varianceRow = new Document("stdDevPop", 10.0);
        var varianceIt = stubIterable(varianceRow);
        when(mongo.aggregate(anyString(), any(List.class))).thenReturn(varianceIt);
        variance.aggregate(query());
        verify(mongo, never()).find(anyString(), any(Bson.class));
    }
}
