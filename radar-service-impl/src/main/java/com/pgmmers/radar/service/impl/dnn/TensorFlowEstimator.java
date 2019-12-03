package com.pgmmers.radar.service.impl.dnn;

import com.pgmmers.radar.service.dnn.Estimator;
import com.pgmmers.radar.service.model.MoldService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class TensorFlowEstimator implements Estimator {
    @Resource
    private MoldService moldService;

    @Override
    public double predict(Long modelId, Map<String, Map<String, ?>> data) {
        return 0;
    }
}
