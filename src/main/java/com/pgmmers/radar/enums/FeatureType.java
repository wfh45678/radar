package com.pgmmers.radar.enums;

/**
 * 特征类型（v4.1 特征工程重构）。
 *
 * <p>预处理（PreItem）与抽象（Abstraction）统一为特征（Feature）体系，
 * DB 存储枚举名（engine_feature.TYPE 列），执行层由
 * {@code FeatureExecutorRegistry} 按 type 分发到对应 {@code FeatureExecutor}。
 * 扩展新特征类型：加枚举值 + 新 Executor bean，引擎编排层无需改动。</p>
 */
public enum FeatureType {

    /** 插件加工：外置/内置插件对事件字段加工（原 v3.x PreItem），支持多列输出，可链式读取前序特征 */
    ENRICH("插件加工"),

    /** 衍生表达式：事件字段与前序特征的表达式运算（算术/字符串函数） */
    DERIVED("衍生表达式"),

    /** 统计聚合：时间窗内 Mongo 事件统计（原 v3.x Abstraction），全量拉内存型函数见各实现标注 */
    AGGREGATE("统计聚合"),

    /** 组合比率：两个统计特征的组合运算（如 金额/笔数=客单价），带除零策略 */
    RATIO("组合比率");

    private final String label;

    FeatureType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 解析 DB 存储值；空/未知返回 null（编排层按特征隔离跳过并告警）。
     */
    public static FeatureType of(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        try {
            return valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
