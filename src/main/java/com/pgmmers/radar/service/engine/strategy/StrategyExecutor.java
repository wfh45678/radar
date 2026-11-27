package com.pgmmers.radar.service.engine.strategy;

import com.pgmmers.radar.enums.StrategyType;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import java.util.Map;

/**
 * 策略执行器 SPI（v4.0 策略板块重构）。
 *
 * <p>每种策略类型一个实现（Spring bean），启动时由 {@code StrategyExecutorRegistry}
 * 收集注册（范式对齐 PluginManager）。引擎编排层只遍历 ACTIVE 策略并按 type 分发，
 * 不感知类型内语义——扩展新策略类型无需改动编排层。</p>
 *
 * <p>实现约定：</p>
 * <ul>
 *   <li>求值上下文 data：{fields: 事件字段, preItems: 预处理输出, abstractions: 统计指标}；</li>
 *   <li>lists：预加载名单集合（lists.&lt;name&gt; 为 Set 形态）；</li>
 *   <li>条件求值统一走 {@code ConditionEvaluator}（后端解释 ruleDefinition 同构 JSON），不再前端生成脚本；</li>
 *   <li>异常向上抛出，由编排层按策略隔离（跳过该策略、不影响其他策略与事件成功）。</li>
 * </ul>
 */
public interface StrategyExecutor {

    /** 本执行器服务的策略类型 */
    StrategyType type();

    /**
     * 执行单个策略。
     *
     * @param strategy 策略配置（type=本执行器类型）
     * @param data     求值上下文
     * @param lists    预加载名单集合
     * @return 得分 + 决策 + 命中明细
     */
    StrategyOutcome execute(StrategyVO strategy, Map<String, Map<String, ?>> data, Map<String, ?> lists);
}
