package com.pgmmers.radar.service.engine.feature;

import com.pgmmers.radar.enums.AggregateKind;

/**
 * 聚合函数 SPI（v4.1 特征工程）：替换 v3.x AggregateCommand 的 switch-case 分发。
 * 每种统计类型一个实现（Spring bean），由 AggregateFunctionRegistry 按 {@link AggregateKind} 收集。
 * 扩展新统计类型（如分位数/近似去重）= 枚举值 + 新实现 bean，执行器零改动。
 *
 * <p>约定：</p>
 * <ul>
 *   <li>时间过滤统一 refDateFieldName ∈ [begin, end]（毫秒 epoch）；</li>
 *   <li>统计口径统一过滤 status='1'（v4.1 起，与 COUNT 对齐——v3.x 仅 COUNT 过滤）；</li>
 *   <li>空数据集语义由实现自定（数值族建议返回 0 或 null，由执行器决定写入与否）；</li>
 *   <li>MEDIAN/VARIANCE/SD 为全量拉内存实现，大数据量场景注意（近似算法列 backlog）。</li>
 * </ul>
 */
public interface AggregateFunction {

    /** 本实现对应的统计类型 */
    AggregateKind kind();

    /**
     * 执行统计。
     *
     * @return 统计结果（数值/标量）；无数据可返回 null（编排层不写入）
     */
    Object aggregate(AggregateQuery query);
}
