package com.pgmmers.radar.service.engine.vo;


import com.pgmmers.radar.service.common.CommonResult;

import java.util.HashMap;
import java.util.Map;

public class AdaptationResult extends CommonResult {

    private Map<String, Object> adaptationMap = new HashMap<String, Object>();

    public Map<String, Object> getAdaptationMap() {
        return adaptationMap;
    }

    public void setAdaptationMap(Map<String, Object> adaptationMap) {
        this.adaptationMap = adaptationMap;
    }




}
