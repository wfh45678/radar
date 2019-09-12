package com.pgmmers.radar.service.cache;



import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.model.*;

import java.io.Serializable;
import java.util.List;

public interface CacheService {

     void saveModel(ModelVO model);

     ModelVO getModel(Long modelId);

     void saveField(FieldVO field);

     List<FieldVO> listFields(Long modelId);

     void saveAbstraction(AbstractionVO abstraction);

     List<AbstractionVO> listAbstraction(Long modelId);

     void saveActivation(ActivationVO activation);

     List<ActivationVO> listActivation(Long modelId);

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

}
