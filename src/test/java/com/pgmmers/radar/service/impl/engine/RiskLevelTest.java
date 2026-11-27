package com.pgmmers.radar.service.impl.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pgmmers.radar.service.impl.engine.strategy.RuleSetExecutor;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * L1 纯单测：规则集风险级别决策（v4.0 起逻辑位于 RuleSetExecutor）。
 * 阈值三段：pass / review / reject。
 * 边界约定：sum ≥ high 即 reject（即使也 ≥ median）；sum 恰等于 median 为 review。
 */
class RiskLevelTest {

    private static BigDecimal sum(int score) {
        return new BigDecimal(score);
    }

    @Test
    void belowMedianPasses() {
        assertEquals("pass", RuleSetExecutor.riskLevel(sum(40), 100, 50));
    }

    @Test
    void atMedianIsReview() {
        assertEquals("review", RuleSetExecutor.riskLevel(sum(50), 100, 50));
    }

    @Test
    void betweenMedianAndHighIsReview() {
        assertEquals("review", RuleSetExecutor.riskLevel(sum(99), 100, 50));
    }

    @Test
    void atHighIsReject() {
        assertEquals("reject", RuleSetExecutor.riskLevel(sum(100), 100, 50));
    }

    @Test
    void aboveHighIsReject() {
        assertEquals("reject", RuleSetExecutor.riskLevel(sum(200), 100, 50));
    }

    @Test
    void decimalSumComparesByValue() {
        // setScale(0) 后应无小数；此处验证 BigDecimal 按值比较而非按精度
        assertEquals("review", RuleSetExecutor.riskLevel(new BigDecimal("50.0"), 100, 50));
    }
}
