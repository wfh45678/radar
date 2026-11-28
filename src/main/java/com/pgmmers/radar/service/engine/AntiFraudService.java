package com.pgmmers.radar.service.engine;

import com.pgmmers.radar.service.common.CommonResult;

import java.util.Map;

public interface AntiFraudService {

     /**
      * 风险分析。
      * @param modelId model id
      * @param context data context
      * @return
      * @author feihu.wang
      */
     CommonResult process(Long modelId, Map<String, Map<String, ?>> context);

     /**
      * 入库前特征执行（v4.1：ENRICH 插件加工，默认失败隔离）。
      * @param modelId model id
      * @param jsonInfo 事件 JSON
      * @return 特征输出（featureName → 值/多列 Map），随事件落库
      */
     Map<String, Object> executeEntryFeatures(Long modelId, Map<String, Object> jsonInfo);

}
