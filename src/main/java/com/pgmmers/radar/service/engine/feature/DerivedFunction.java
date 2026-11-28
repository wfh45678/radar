package com.pgmmers.radar.service.engine.feature;

import java.util.List;

/**
 * 衍生表达式函数 SPI（v4.1 特征工程）：新增函数 = 实现本接口注册为 Spring bean，
 * {@code ExpressionEvaluator} 启动时自动收集（内置函数与自定义函数同机制）。
 *
 * <p>函数按名字唯一（重复注册首个生效并告警）；参数已在求值期解析为
 * BigDecimal / String 混合列表，实现自行做类型判定，无法计算时返回 null（特征无产出）。</p>
 */
public interface DerivedFunction {

    /** 函数名（DEFINITION.expression.fn，大小写不敏感） */
    String name();

    /** 参数数量下限（含） */
    int minArgs();

    /** 参数数量上限（含）；-1 表示不限定 */
    int maxArgs();

    /**
     * 执行函数。
     *
     * @param args 已求值参数（BigDecimal 或 String，可能为 null）
     * @return 结果（BigDecimal/String）或 null（无产出）
     */
    Object apply(List<Object> args);
}
