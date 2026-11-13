package com.pgmmers.radar.service.engine.vo;


import com.pgmmers.radar.service.common.CommonResult;

import java.util.HashMap;
import java.util.Map;

public class AbstractionResult extends CommonResult {

    private Map<String, Object> abstractionMap = new HashMap<>();

    public Map<String, Object> getAbstractionMap() {
        return abstractionMap;
    }

    public void setAbstractionMap(Map<String, Object> abstractionMap) {
        this.abstractionMap = abstractionMap;
    }


}
