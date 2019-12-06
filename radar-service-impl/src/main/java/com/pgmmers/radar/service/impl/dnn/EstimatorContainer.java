package com.pgmmers.radar.service.impl.dnn;

import com.pgmmers.radar.service.dnn.Estimator;
import com.pgmmers.radar.service.model.MoldService;
import com.pgmmers.radar.vo.model.MoldVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class EstimatorContainer {

    private Map<String, Estimator> estimatorMap = new HashMap<>();

    @Resource
    private MoldService moldService;

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
        MoldVO mold = moldService.getByModelId(modelId);
        if (mold == null) {
            return null;
        }
        return getByType(mold.getType());
    }
}
