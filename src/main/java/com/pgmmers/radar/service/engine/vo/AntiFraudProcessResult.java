package com.pgmmers.radar.service.engine.vo;


import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pgmmers.radar.service.common.CommonResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiFraudProcessResult extends CommonResult {

    private Map<String, ?> abstractions;

    private Map<String, ?> adaptations;

    /**
     * 策略执行结果（v4.0 起"激活器"更名"策略"；字段名保持 activations 以兼容引擎接口调用方与 ES 报表）。
     */
    @JsonProperty("activations")
    @JSONField(name = "activations")
    private Map<String, ?> strategies;
    
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

    public Map<String, ?> getStrategies() {
        return strategies;
    }

    public void setStrategies(Map<String, ?> strategies) {
        this.strategies = strategies;
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
