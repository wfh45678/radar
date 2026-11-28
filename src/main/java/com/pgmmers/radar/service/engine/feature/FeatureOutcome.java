package com.pgmmers.radar.service.engine.feature;

/**
 * 单个特征的执行结果（v4.1 特征 SPI）：value 写入 features.&lt;featureName&gt;。
 * 单值特征为标量；多列特征（ENRICH 插件 Map 产出）为嵌套 Map。
 * value 为 null 表示本特征无产出（条件不命中/无数据），编排层跳过写入。
 */
public class FeatureOutcome {

    private final Object value;

    private FeatureOutcome(Object value) {
        this.value = value;
    }

    public static FeatureOutcome of(Object value) {
        return new FeatureOutcome(value);
    }

    /** 无产出（不写入上下文） */
    public static FeatureOutcome empty() {
        return new FeatureOutcome(null);
    }

    public Object getValue() {
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }
}
