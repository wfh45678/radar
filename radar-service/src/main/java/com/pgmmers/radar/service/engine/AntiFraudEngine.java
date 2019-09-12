package com.pgmmers.radar.service.engine;


import com.pgmmers.radar.service.engine.vo.AbstractionResult;
import com.pgmmers.radar.service.engine.vo.ActivationResult;
import com.pgmmers.radar.service.engine.vo.AdaptationResult;

import java.util.Map;

public interface AntiFraudEngine {

     AbstractionResult executeAbstraction(Long modelId, Map<String, Map<String, ?>> data);

     AdaptationResult executeAdaptation(Long modelId, Map<String, Map<String, ?>> data);

     ActivationResult executeActivation(Long modelId, Map<String, Map<String, ?>> data);

}
