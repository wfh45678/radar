package com.pgmmers.radar.service.impl.dnn;

import com.pgmmers.radar.service.dnn.Estimator;
import com.pgmmers.radar.service.model.ModelConfService;
import com.pgmmers.radar.model.vo.model.ModelConfVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class EstimatorContainer {

    private Map<String, Estimator> estimatorMap = new HashMap<>();

    @Resource
    private ModelConfService modelConfService;

    /**
     * TensorFlow DNN 移除后暂无 Estimator 实现（required=false 容许空集），
     * 后续基于 TF-Java 或外部推理服务重接时自动注入。
     */
    @Autowired(required = false)
    public void set(Estimator[] estimators) {
        if (estimators == null) {
            return;
        }
        for (Estimator estimator : estimators) {
            estimatorMap.put(estimator.getType(), estimator);
        }
    }

    public Estimator getByType(String type) {
        return estimatorMap.get(type);
    }

    public Estimator getByScenarioId(Long scenarioId) {
        ModelConfVO mold = modelConfService.getByScenarioId(scenarioId);
        if (mold == null) {
            return null;
        }
        return getByType(mold.getType());
    }
}
