package com.pgmmers.radar.enums;

/**
 * 统计聚合类型（v4.1 特征工程重构）：取代 v3.x 的 int 常量类 {@code AggregateType}，
 * DB（engine_feature.DEFINITION.aggregateType）存储枚举名，code 保留与旧 int 值的映射
 * （历史数据/模板迁移用）。
 *
 * <p>执行由 {@code AggregateFunction} 注册表按 kind 分发（替换原 switch-case）；
 * 扩展新聚合 = 新增枚举值 + 注册 AggregateFunction bean。</p>
 */
public enum AggregateKind {

    COUNT("计数", 1),
    DISTINCT_COUNT("去重计数", 2),
    SUM("求和", 3),
    AVERAGE("平均", 4),
    MAX("最大", 5),
    MIN("最小", 6),
    /** 标准差 */
    SD("标准差", 7),
    /** 方差 */
    VARIANCE("方差", 8),
    /** 偏离率：|本次事件值 - 均值| × 100 / 均值 */
    DEVIATION("偏离率", 9),
    /** 中位数（全量拉内存，性能注意） */
    MEDIAN("中位数", 10);

    private final String label;
    private final int code;

    AggregateKind(String label, int code) {
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    /** 旧 int 常量（AggregateType 1-10）映射 */
    public int getCode() {
        return code;
    }

    /** 解析存储值；空/未知返回 null（调用方按特征隔离处理） */
    public static AggregateKind of(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 旧 int 常量反查（v3.x 数据/模板迁移用）；未知返回 null */
    public static AggregateKind ofCode(int code) {
        for (AggregateKind kind : values()) {
            if (kind.code == code) {
                return kind;
            }
        }
        return null;
    }
}
