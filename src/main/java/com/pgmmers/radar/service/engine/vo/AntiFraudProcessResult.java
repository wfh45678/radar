package com.pgmmers.radar.service.engine.vo;


import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pgmmers.radar.service.common.CommonResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiFraudProcessResult extends CommonResult {

    /**
     * 特征执行结果（v4.1 特征工程：合并原 preItems+abstractions，键 features）。
     */
    private Map<String, ?> features;

    private Map<String, ?> adaptations;

    /**
     * 策略执行结果（v4.1：响应键由 v4.0 的 activations 更名 strategies，
     * 同步修复引擎写 ES 与报表/导出读键的历史失配；调用方需按升级指南调整）。
     */
    private Map<String, ?> strategies;
    
    private Map<String, List<HitObject>> hitsDetail = new HashMap<>();
    
    private Map<String, Object> respTimes = new HashMap<>();

    public Map<String, ?> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, ?> features) {
        this.features = features;
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
