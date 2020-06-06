package com.pgmmers.radar.service.cache;


import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.model.AbstractionVO;
import com.pgmmers.radar.vo.model.ActivationVO;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import com.pgmmers.radar.vo.model.DataListsVO;
import com.pgmmers.radar.vo.model.FieldVO;
import com.pgmmers.radar.vo.model.ModelVO;
import com.pgmmers.radar.vo.model.PreItemVO;
import com.pgmmers.radar.vo.model.RuleVO;


public interface CacheService {
     void saveAntiFraudResult(String modelId, String sessionId, CommonResult result);

     CommonResult getAntiFraudResult(String modelId, String sessionId);

     void publishModel(ModelVO model);

     void subscribeModel(SubscribeHandle handler);

     void publishField(FieldVO field);

     void subscribeField(SubscribeHandle handler);

     void publishPreItem(PreItemVO preItem);

     void subscribePreItem(SubscribeHandle handler);

     void publishAbstraction(AbstractionVO model);

     void subscribeAbstraction(SubscribeHandle handler);

     void publishRule(RuleVO rule);

     void subscribeRule(SubscribeHandle handler);

     void publishActivation(ActivationVO activation);

     void subscribeActivation(SubscribeHandle handler);

     void publishDataListRecord(DataListRecordVO record);
     void subscribeDataListRecord(SubscribeHandle handler);

     void publishDataList(DataListsVO dataList);
     void subscribeDataList(SubscribeHandle handler);


     void cacheCaptcha(String captcha);

     boolean validateCaptcha(String captcha);
}
