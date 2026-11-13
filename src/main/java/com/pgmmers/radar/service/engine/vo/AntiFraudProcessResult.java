package com.pgmmers.radar.service.engine.vo;


import com.pgmmers.radar.service.common.CommonResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiFraudProcessResult extends CommonResult {

    private Map<String, ?> abstractions;

    private Map<String, ?> adaptations;
    
    private Map<String, ?> activations;
    
    private Map<String, List<HitObject>> hitsDetail = new HashMap<>();
    
    private Map<String, Object> respTimes = new HashMap<>();

    public Map<String, ?> getAbstractions() {
        return abstractions;
    }

    public void setAbstractions(Map<String, ?> abstractions) {
        this.abstractions = abstractions;
    }

    public Map<String, ?> getAdaptations() {
        return adaptations;
    }

    public void setAdaptations(Map<String, ?> adaptations) {
        this.adaptations = adaptations;
    }

    public Map<String, ?> getActivations() {
        return activations;
    }

    public void setActivations(Map<String, ?> activations) {
        this.activations = activations;
    }

    public Map<String, Object> getRespTimes() {
        return respTimes;
    }

    public void setRespTimes(Map<String, Object> respTimes) {
        this.respTimes = respTimes;
    }

    public Map<String, List<HitObject>> getHitsDetail() {
        return hitsDetail;
    }

    public void setHitsDetail(Map<String, List<HitObject>> hitsDetail) {
        this.hitsDetail = hitsDetail;
    }

}
