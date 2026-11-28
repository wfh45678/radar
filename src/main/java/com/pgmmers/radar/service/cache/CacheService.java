package com.pgmmers.radar.service.cache;


import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.model.vo.model.DataListRecordVO;
import com.pgmmers.radar.model.vo.model.DataListsVO;
import com.pgmmers.radar.model.vo.model.FieldVO;
import com.pgmmers.radar.model.vo.model.ModelVO;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.model.vo.model.RuleVO;

/**
 * cache service.
 * @author feihu.wang
 */
public interface CacheService {
     void saveAntiFraudResult(String modelId, String sessionId, CommonResult result);

     CommonResult getAntiFraudResult(String modelId, String sessionId);

     void publishModel(ModelVO model);

     void subscribeModel(SubscribeHandle handler);

     void publishField(FieldVO field);

     void subscribeField(SubscribeHandle handler);

     void publishFeature(FeatureVO feature);

     void subscribeFeature(SubscribeHandle handler);

     void publishRule(RuleVO rule);

     void subscribeRule(SubscribeHandle handler);

     void publishStrategy(StrategyVO strategy);

     void subscribeStrategy(SubscribeHandle handler);

     void publishDataListRecord(DataListRecordVO record);
     void subscribeDataListRecord(SubscribeHandle handler);

     void publishDataList(DataListsVO dataList);
     void subscribeDataList(SubscribeHandle handler);


     void cacheCaptcha(String captcha);

     boolean validateCaptcha(String captcha);
}
