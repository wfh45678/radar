package com.pgmmers.radar.service.impl.dnn;

import com.pgmmers.radar.service.dnn.Estimator;
import com.pgmmers.radar.service.model.ModelConfService;
import com.pgmmers.radar.vo.model.ModelConfVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class EstimatorContainer {

    private Map<String, Estimator> estimatorMap = new HashMap<>();

    @Resource
    private ModelConfService modelConfService;

    @Autowired
    public void set(Estimator[] estimators) {
        for (Estimator estimator : estimators) {
            estimatorMap.put(estimator.getType(), estimator);
        }
    }

    public Estimator getByType(String type) {
        return estimatorMap.get(type);
    }

    public Estimator getByModelId(Long modelId) {
        ModelConfVO mold = modelConfService.getByModelId(modelId);
        if (mold == null) {
            return null;
        }
        return getByType(mold.getType());
    }
}
