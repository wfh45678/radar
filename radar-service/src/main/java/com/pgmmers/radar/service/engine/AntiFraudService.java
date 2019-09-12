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
      * 预处理数据。
      * @param modelId model id
      * @param jsonInfo data context json
      * @return
      * @author feihu.wang
      */
     Map<String, Object> prepare(Long modelId, Map<String, Object> jsonInfo);

}
