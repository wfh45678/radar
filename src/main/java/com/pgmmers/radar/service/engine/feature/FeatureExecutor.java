package com.pgmmers.radar.service.engine.feature;

import com.pgmmers.radar.enums.FeatureStage;
import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import java.util.Map;

/**
 * 特征执行器 SPI（v4.1 特征工程重构）：每种特征类型一个实现（Spring bean），
 * 启动时由 {@code FeatureExecutorRegistry} 收集注册（范式对齐 PluginManager / StrategyExecutor）。
 *
 * <p>引擎编排层按 {@link FeatureStage} 遍历该阶段类型集合内的 ACTIVE 特征（阶段内按 ORDER_NO
 * 排序）并分发，不感知类型内语义——扩展新特征类型 = 枚举值 + 新 Executor bean + 挂入对应阶段。</p>
 *
 * <p>实现约定：</p>
 * <ul>
 *   <li>ENTRY 阶段（ENRICH/DERIVED）在事件实体落库前执行，产出随事件进 Mongo/ES；</li>
 *   <li>ANALYSIS 阶段（AGGREGATE/RATIO）在事件落库后执行，可按特征值检索历史事件；</li>
 *   <li>列引用统一 features.&lt;name&gt;[.&lt;column&gt;]（多列特征走点路径），经
 *       {@code ConditionEvaluator.resolveColumn} 解析；</li>
 *   <li>异常向上抛出，由编排层按特征隔离（跳过该特征、不影响事件链路）。</li>
 * </ul>
 */
public interface FeatureExecutor {

    /** 本执行器服务的特征类型 */
    FeatureType type();

    /**
     * 执行单个特征。
     *
     * @param feature 特征配置（type=本执行器类型）
     * @param data    求值上下文 {fields, features(累积), ...}
     * @param lists   预加载名单集合（name → Map，containsKey 语义）
     * @return 特征产出；empty() 表示无产出（不写入上下文）
     */
    FeatureOutcome execute(FeatureVO feature, Map<String, Map<String, ?>> data, Map<String, ?> lists);
}
