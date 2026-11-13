package com.pgmmers.radar.service.engine.vo;


import com.pgmmers.radar.service.common.CommonResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivationResult extends CommonResult {

    private Map<String, Object> activationMap = new HashMap<String, Object>();

    public Map<String, Object> getActivationMap() {
        return activationMap;
    }

    public void setActivationMap(Map<String, Object> activationMap) {
        this.activationMap = activationMap;
    }


    public Map<String, List<HitObject>> getHitRulesMap() {
        return hitRulesMap;
    }

    public void setHitRulesMap(Map<String, List<HitObject>> hitRulesMap) {
        this.hitRulesMap = hitRulesMap;
    }






    public Map<String, Map<String, HitObject>> getHitRulesMap2() {
		return hitRulesMap2;
	}

	public void setHitRulesMap2(Map<String, Map<String, HitObject>> hitRulesMap2) {
		this.hitRulesMap2 = hitRulesMap2;
	}






	private Map<String, List<HitObject>> hitRulesMap = new HashMap<>();
    
    private Map<String, Map<String, HitObject>> hitRulesMap2 = new HashMap<>();


}
