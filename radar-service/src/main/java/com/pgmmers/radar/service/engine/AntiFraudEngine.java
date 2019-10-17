package com.pgmmers.radar.service.engine;


import com.pgmmers.radar.service.engine.vo.AbstractionResult;
import com.pgmmers.radar.service.engine.vo.ActivationResult;
import com.pgmmers.radar.service.engine.vo.AdaptationResult;

import java.util.Map;

/**
 * 风控引擎接口。
 * @author feihu.wang
 */
public interface AntiFraudEngine {

     /**
      * 特征/抽象指标的计算提取。
      * @param modelId
      * @param data
      * @return
      */
     AbstractionResult executeAbstraction(Long modelId, Map<String, Map<String, ?>> data);

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
     ActivationResult executeActivation(Long modelId, Map<String, Map<String, ?>> data);

}
