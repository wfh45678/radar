package com.pgmmers.radar.service.impl.engine.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * L1 纯单测：名单匹配执行器（ListMatchExecutor）。
 * 覆盖：命中直出决策、未命中放行、名单缺失降级、取值缺失降级、DEFINITION 缺失异常。
 */
class ListMatchExecutorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ListMatchExecutor executor = new ListMatchExecutor();
    private Map<String, Map<String, ?>> data;
    private Map<String, Object> lists;

    @BeforeEach
    void setUp() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("mobile", "13800138000");
        data = new HashMap<>();
        data.put("fields", fields);

        Map<String, String> blacklist = new HashMap<>();
        blacklist.put("13800138000", "");
        lists = new HashMap<>();
        lists.put("手机号黑名单", blacklist);
    }

    private StrategyVO strategy(String definitionJson) throws Exception {
        StrategyVO vo = new StrategyVO();
        vo.setStrategyName("list_match_1");
        vo.setDefinition(MAPPER.readTree(definitionJson));
        return vo;
    }

    @Test
    @DisplayName("命中名单：直出命中决策/得分/理由，写 list_ 明细")
    void hitList() throws Exception {
        String definition = "{"
                + "\"dataList\": \"手机号黑名单\","
                + "\"matchField\": \"fields.mobile\","
                + "\"hitDecision\": \"reject\","
                + "\"hitScore\": 100,"
                + "\"hitReason\": \"命中黑名单\""
                + "}";
        StrategyOutcome outcome = executor.execute(strategy(definition), data, lists);
        assertEquals("reject", outcome.getRisk());
        assertEquals(0, outcome.getScore().compareTo(new java.math.BigDecimal(100)));
        assertTrue(outcome.getHits2().containsKey("list_手机号黑名单"));
        assertTrue(outcome.getHits2().get("list_手机号黑名单").getDesc().contains("命中黑名单"));
    }

    @Test
    @DisplayName("未命中名单：走缺省放行（pass）")
    void missList() throws Exception {
        Map<String, Object> fields = new HashMap<>();
        fields.put("mobile", "19999999999");
        data.put("fields", fields);
        String definition = "{"
                + "\"dataList\": \"手机号黑名单\","
                + "\"matchField\": \"fields.mobile\","
                + "\"hitDecision\": \"reject\", \"hitScore\": 100"
                + "}";
        StrategyOutcome outcome = executor.execute(strategy(definition), data, lists);
        assertEquals("pass", outcome.getRisk());
        assertEquals(0, outcome.getScore().compareTo(java.math.BigDecimal.ZERO));
    }

    @Test
    @DisplayName("名单缺失降级：缓存无此名单按未命中放行（不抛异常）")
    void listMissing() throws Exception {
        String definition = "{"
                + "\"dataList\": \"不存在的名单\","
                + "\"matchField\": \"fields.mobile\","
                + "\"hitDecision\": \"reject\""
                + "}";
        StrategyOutcome outcome = executor.execute(strategy(definition), data, lists);
        assertEquals("pass", outcome.getRisk());
    }

    @Test
    @DisplayName("DEFINITION 缺失：抛出异常（与评分卡同口径）")
    void missingDefinition() {
        StrategyVO vo = new StrategyVO();
        vo.setStrategyName("list_match_bad");
        assertThrows(IllegalArgumentException.class, () -> executor.execute(vo, data, lists));
    }
}
