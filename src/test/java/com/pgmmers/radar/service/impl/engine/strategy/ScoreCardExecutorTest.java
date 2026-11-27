package com.pgmmers.radar.service.impl.engine.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.service.engine.strategy.StrategyOutcome;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * L1 纯单测：评分卡执行器（ScoreCardExecutor）。
 * 覆盖：initialScore、分箱首中生效、权重、defaultScore、InList 名单分箱、阈值决策。
 */
class ScoreCardExecutorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ScoreCardExecutor executor = new ScoreCardExecutor();
    private Map<String, Map<String, ?>> data;
    private Map<String, Object> lists;

    @BeforeEach
    void setUp() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("age", 22);
        Map<String, Object> abstractions = new HashMap<>();
        abstractions.put("tran_ip_1_day_qty", 35);
        abstractions.put("amt_1_day_sum", 9800.0);
        data = new HashMap<>();
        data.put("fields", fields);
        data.put("abstractions", abstractions);

        Map<String, String> highRiskArea = new HashMap<>();
        highRiskArea.put("OVERSEAS", "");
        lists = new HashMap<>();
        lists.put("high_risk_area", highRiskArea);
    }

    private StrategyVO strategy(String definitionJson, int high, int median) throws Exception {
        StrategyVO vo = new StrategyVO();
        vo.setStrategyName("scorecard_1");
        vo.setHigh(high);
        vo.setMedian(median);
        vo.setDefinition(MAPPER.readTree(definitionJson));
        return vo;
    }

    @Test
    @DisplayName("基础计分：initialScore + 分箱首中 + 权重")
    void basicScoring() throws Exception {
        String definition = "{"
                + "\"initialScore\": 600,"
                + "\"features\": ["
                + "  {\"name\":\"f_age\",\"label\":\"年龄\",\"column\":\"fields.age\",\"weight\":1.0,\"defaultScore\":0,"
                + "   \"bins\":["
                + "     {\"label\":\"<18\",\"operator\":\"Less\",\"value\":\"18\",\"score\":-20},"
                + "     {\"label\":\"18-25\",\"operator\":\"Less_Equal\",\"value\":\"25\",\"score\":-10}"
                + "   ]},"
                + "  {\"name\":\"f_ip\",\"label\":\"IP频次\",\"column\":\"abstractions.tran_ip_1_day_qty\",\"weight\":2.0,\"defaultScore\":5,"
                + "   \"bins\":["
                + "     {\"label\":\">=30\",\"operator\":\"Greater_Equal\",\"value\":\"30\",\"score\":30}"
                + "   ]}"
                + "]}";
        StrategyOutcome outcome = executor.execute(strategy(definition, 800, 650), data, lists);
        // 600 + (-10 × 1.0) + (30 × 2.0) = 650
        assertEquals(650, outcome.getScore().intValue());
        assertEquals("review", outcome.getRisk());
        assertEquals(2, outcome.getHits().size());
        assertEquals("年龄:18-25", outcome.getHits().get(0).getDesc());
        assertEquals(60.0, outcome.getHits().get(1).getValue());
        assertEquals("f_ip", outcome.getHits2().get("feature_f_ip").getKey());
    }

    @Test
    @DisplayName("defaultScore：无命中分箱时生效")
    void defaultScore() throws Exception {
        String definition = "{"
                + "\"initialScore\": 0,"
                + "\"features\": ["
                + "  {\"name\":\"f_age\",\"label\":\"年龄\",\"column\":\"fields.age\",\"weight\":1.0,\"defaultScore\":15,"
                + "   \"bins\":[{\"label\":\"<18\",\"operator\":\"Less\",\"value\":\"18\",\"score\":-20}]}"
                + "]}";
        StrategyOutcome outcome = executor.execute(strategy(definition, 100, 50), data, lists);
        assertEquals(15, outcome.getScore().intValue());
        assertEquals("年龄:default", outcome.getHits().get(0).getDesc());
        assertEquals("pass", outcome.getRisk());
    }

    @Test
    @DisplayName("阈值决策：>=high 拒绝、>=medium 审核、否则通过")
    void thresholdDecision() throws Exception {
        String definition = "{"
                + "\"initialScore\": 0,"
                + "\"features\": ["
                + "  {\"name\":\"f_ip\",\"label\":\"IP频次\",\"column\":\"abstractions.tran_ip_1_day_qty\",\"weight\":1.0,\"defaultScore\":0,"
                + "   \"bins\":[{\"label\":\">=30\",\"operator\":\"Greater_Equal\",\"value\":\"30\",\"score\":120}]}"
                + "]}";
        assertEquals("reject", executor.execute(strategy(definition, 100, 50), data, lists).getRisk());
        assertEquals("review", executor.execute(strategy(definition, 200, 100), data, lists).getRisk());
        assertEquals("pass", executor.execute(strategy(definition, 300, 200), data, lists).getRisk());
    }

    @Test
    @DisplayName("InList 名单分箱")
    void listBin() throws Exception {
        Map<String, Object> preItems = new HashMap<>();
        preItems.put("country", "OVERSEAS");
        data.put("preItems", preItems);
        String definition = "{"
                + "\"initialScore\": 0,"
                + "\"features\": ["
                + "  {\"name\":\"f_area\",\"label\":\"地区\",\"column\":\"preItems.country\",\"weight\":1.0,\"defaultScore\":0,"
                + "   \"bins\":[{\"label\":\"高危地区\",\"operator\":\"InList\",\"value\":\"high_risk_area\",\"score\":80}]}"
                + "]}";
        StrategyOutcome outcome = executor.execute(strategy(definition, 100, 50), data, lists);
        assertEquals(80, outcome.getScore().intValue());
        assertEquals("地区:高危地区", outcome.getHits().get(0).getDesc());
    }

    @Test
    @DisplayName("definition 缺失抛出异常（由编排层隔离）")
    void missingDefinition() {
        StrategyVO vo = new StrategyVO();
        vo.setStrategyName("broken");
        assertThrows(IllegalArgumentException.class,
                () -> executor.execute(vo, data, lists));
    }

    @Test
    @DisplayName("空特征列表：仅 initialScore")
    void emptyFeatures() throws Exception {
        StrategyOutcome outcome = executor.execute(strategy("{\"initialScore\": 300,\"features\": []}", 400, 350), data, lists);
        assertEquals(300, outcome.getScore().intValue());
        assertEquals("pass", outcome.getRisk());
        assertTrue(outcome.getHits().isEmpty());
    }
}
