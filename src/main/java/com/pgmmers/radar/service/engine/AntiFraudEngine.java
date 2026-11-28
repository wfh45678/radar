package com.pgmmers.radar.service.engine;


import com.pgmmers.radar.service.engine.vo.FeatureResult;
import com.pgmmers.radar.service.engine.vo.StrategyResult;
import com.pgmmers.radar.service.engine.vo.AdaptationResult;

import java.util.Map;

/**
 * 风控引擎接口。
 * @author feihu.wang
 */
public interface AntiFraudEngine {

     /**
      * 特征执行（v4.1 特征工程）：按阶段执行 ENRICH/DERIVED（ENTRY，落库前）
      * 或 AGGREGATE/RATIO（ANALYSIS，落库后），结果合并进 data 的 features 命名空间。
      * @param modelId 模型 id
      * @param data 求值上下文
      * @param stage 执行阶段
      * @return 本阶段特征结果
      */
     FeatureResult executeFeatures(Long modelId, Map<String, Map<String, ?>> data, com.pgmmers.radar.enums.FeatureStage stage);

     /**
      * 机器学习适配器，用于支持机器学习，获取风险评分，此接口预留。
      * @param modelId
      * @param data
      * @return
      */
     AdaptationResult executeAdaptation(Long modelId, Map<String, Map<String, ?>> data);

     /**
      * 激活器/策略集 模型的输出点，根据风险分数输出结果，拒绝（reject）或者人工审核(review),或者通过（pass）.
      * @param modelId
      * @param data
      * @return
      */
     StrategyResult executeStrategy(Long modelId, Map<String, Map<String, ?>> data);

}
