package com.pgmmers.radar.service.impl.engine.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.service.engine.strategy.StrategyOutcome;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * L1 纯单测：决策表执行器（DecisionTableExecutor）。
 * 覆盖：行优先首中直出、行内 AND 条件、缺省决策、行命中明细（row_*）。
 */
class DecisionTableExecutorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final DecisionTableExecutor executor = new DecisionTableExecutor();
    private Map<String, Map<String, Object>> data;

    @BeforeEach
    void setUp() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("age", 22);
        Map<String, Object> features = new HashMap<>();
        features.put("tran_ip_1_day_qty", 35);
        data = new HashMap<String, Map<String, Object>>();
        data.put("fields", fields);
        data.put("features", features);
    }

    private StrategyVO strategy(String definitionJson) throws Exception {
        StrategyVO vo = new StrategyVO();
        vo.setStrategyName("decision_table_1");
        vo.setDefinition(MAPPER.readTree(definitionJson));
        return vo;
    }

    @Test
    @DisplayName("首行全条件命中：直出该行决策并写 row_ 明细")
    void firstRowHit() throws Exception {
        String definition = "{"
                + "\"defaultDecision\": \"pass\", \"defaultScore\": 0,"
                + "\"rows\": ["
                + "  {\"label\": \"高频\", \"conditions\": ["
                + "      {\"column\": \"features.tran_ip_1_day_qty\", \"operator\": \"Greater\", \"value\": \"30\"}],"
                + "   \"decision\": \"reject\", \"score\": 100, \"reason\": \"高频交易\"},"
                + "  {\"label\": \"年轻\", \"conditions\": ["
                + "      {\"column\": \"fields.age\", \"operator\": \"Less\", \"value\": \"30\"}],"
                + "   \"decision\": \"review\", \"score\": 50, \"reason\": \"低龄\"}"
                + "]"
                + "}";
        StrategyOutcome outcome = executor.execute(strategy(definition), (Map) data, Map.of());
        assertEquals("reject", outcome.getRisk());
        assertEquals(0, outcome.getScore().compareTo(new java.math.BigDecimal(100)));
        assertTrue(outcome.getHits2().containsKey("row_0"));
        assertEquals("高频交易", outcome.getHits2().get("row_0").getDesc());
    }


    @Test
    @DisplayName("首行未命中：顺序求值命中第二行")
    void secondRowHit() throws Exception {
        data.get("features").put("tran_ip_1_day_qty", 5);
        String definition = "{"
                + "\"rows\": ["
                + "  {\"label\": \"高频\", \"conditions\": ["
                + "      {\"column\": \"features.tran_ip_1_day_qty\", \"operator\": \"Greater\", \"value\": \"30\"}],"
                + "   \"decision\": \"reject\", \"score\": 100},"
                + "  {\"label\": \"低龄\", \"conditions\": ["
                + "      {\"column\": \"fields.age\", \"operator\": \"Less\", \"value\": \"30\"}],"
                + "   \"decision\": \"review\", \"score\": 50}"
                + "]"
                + "}";
        StrategyOutcome outcome = executor.execute(strategy(definition), (Map) data, Map.of());
        assertEquals("review", outcome.getRisk());
        assertEquals(0, outcome.getScore().compareTo(new java.math.BigDecimal(50)));
        assertTrue(outcome.getHits2().containsKey("row_1"));
    }

    @Test
    @DisplayName("全部行未命中：走缺省决策（default 键明细）")
    void defaultDecision() throws Exception {
        data.get("features").put("tran_ip_1_day_qty", 5);
        data.get("fields").put("age", 40);
        String definition = "{"
                + "\"defaultDecision\": \"pass\", \"defaultScore\": 3,"
                + "\"rows\": ["
                + "  {\"label\": \"高频\", \"conditions\": ["
                + "      {\"column\": \"features.tran_ip_1_day_qty\", \"operator\": \"Greater\", \"value\": \"30\"}],"
                + "   \"decision\": \"reject\", \"score\": 100}"
                + "]"
                + "}";
        StrategyOutcome outcome = executor.execute(strategy(definition), (Map) data, Map.of());
        assertEquals("pass", outcome.getRisk());
        assertEquals(0, outcome.getScore().compareTo(new java.math.BigDecimal(3)));
        assertTrue(outcome.getHits2().containsKey("default"));
    }

    @Test
    @DisplayName("DEFINITION 缺失：抛出异常（与评分卡同口径）")
    void missingDefinition() {
        StrategyVO vo = new StrategyVO();
        vo.setStrategyName("decision_table_bad");
        assertThrows(IllegalArgumentException.class, () -> executor.execute(vo, (Map) data, Map.of()));
    }
}
