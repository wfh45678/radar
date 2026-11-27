package com.pgmmers.radar.service.engine.vo;


import com.pgmmers.radar.service.common.CommonResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrategyResult extends CommonResult {

    private Map<String, Object> strategyMap = new HashMap<String, Object>();

    public Map<String, Object> getStrategyMap() {
        return strategyMap;
    }

    public void setStrategyMap(Map<String, Object> strategyMap) {
        this.strategyMap = strategyMap;
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
