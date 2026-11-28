package com.pgmmers.radar.service.engine.vo;

import com.pgmmers.radar.service.common.CommonResult;
import java.util.HashMap;
import java.util.Map;

/**
 * 特征阶段执行结果（v4.1）：featureMap 键=特征名（多列特征为嵌套 Map），
 * 由编排层合并进 context 的 features 命名空间。
 */
public class FeatureResult extends CommonResult {

    private Map<String, Object> featureMap = new HashMap<>();

    public Map<String, Object> getFeatureMap() {
        return featureMap;
    }

    public void setFeatureMap(Map<String, Object> featureMap) {
        this.featureMap = featureMap;
    }
}
