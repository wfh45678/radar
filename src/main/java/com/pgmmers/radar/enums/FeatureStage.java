package com.pgmmers.radar.enums;

import java.util.List;

/**
 * 特征执行阶段（v4.1 特征工程）：四类特征分两阶段执行，阶段间隐式依赖
 * （加工/衍生产物随事件落库，供聚合按特征值检索；聚合产物供组合比率运算）。
 */
public enum FeatureStage {

    /** 入库前阶段：ENRICH 插件加工 + DERIVED 衍生表达式（产出随事件进 Mongo/ES） */
    ENTRY(List.of(FeatureType.ENRICH, FeatureType.DERIVED)),

    /** 分析阶段：AGGREGATE 统计聚合 + RATIO 组合比率（事件落库后执行） */
    ANALYSIS(List.of(FeatureType.AGGREGATE, FeatureType.RATIO));

    private final List<FeatureType> types;

    FeatureStage(List<FeatureType> types) {
        this.types = types;
    }

    /** 本阶段包含的特征类型 */
    public List<FeatureType> getTypes() {
        return types;
    }
}
