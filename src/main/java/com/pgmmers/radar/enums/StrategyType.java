package com.pgmmers.radar.enums;

/**
 * 策略类型（v4.0 策略板块重构）。
 *
 * <p>DB 存储枚举名（engine_strategy.TYPE 列），执行层由
 * {@code StrategyExecutorRegistry} 按 type 分发到对应 {@code StrategyExecutor}。
 * 扩展新类型（决策表/名单匹配等）：加枚举值 + 新 Executor bean，引擎编排层无需改动。</p>
 */
public enum StrategyType {

    /** 规则集：多规则条件命中求和 + 阈值决策（v3.x 激活器原行为） */
    RULE_SET("规则集"),

    /** 评分卡：特征分箱加权求和 + 阈值决策 */
    SCORECARD("评分卡"),

    /** 决策树：条件节点逐层走路径，叶节点直接决策 */
    DECISION_TREE("决策树");

    private final String label;

    StrategyType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 解析 DB 存储值；空/未知归一为 RULE_SET（兼容 v3.x 存量数据与旧策略包导入）。
     */
    public static StrategyType of(String type) {
        if (type == null || type.isEmpty()) {
            return RULE_SET;
        }
        try {
            return valueOf(type);
        } catch (IllegalArgumentException e) {
            return RULE_SET;
        }
    }
}
