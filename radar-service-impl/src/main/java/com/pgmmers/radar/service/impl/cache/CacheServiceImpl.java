package com.pgmmers.radar.service.impl.cache;

import com.alibaba.fastjson.JSON;

import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.RedisService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CacheServiceImpl implements CacheService {

    public static Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);

    public static final String prefix = "radar_";

    public static final String PUB_SUB_MODEL_CHANNEL = "radar_channel_model";
    public static final String PUB_SUB_FIELD_CHANNEL = "radar_channel_field";
    public static final String PUB_SUB_PREITEM_CHANNEL = "radar_channel_preitem";
    public static final String PUB_SUB_ABSTRACTION_CHANNEL = "radar_channel_abstraction";
    public static final String PUB_SUB_RULE_CHANNEL = "radar_channel_rule";
    public static final String PUB_SUB_ACTIVATION_CHANNEL = "radar_channel_activation";
    public static final String PUB_SUB_LISTRECORD_CHANNEL = "radar_channel_listrecord";
    public static final String PUB_SUB_DATALIST_CHANNEL = "radar_channel_datalist";
    
    @Autowired
    private RedisService redisService;

    @Override
    public void saveModel(ModelVO model) {
        redisService.set(prefix + "model_" + model.getId(), model);
    }

    @Override
    public ModelVO getModel(Long modelId) {
        return redisService.get(prefix + "model_" + modelId, ModelVO.class);
    }

    @Override
    public void saveField(FieldVO field) {
        String key = prefix + "field_" + field.getModelId();
        redisService.hset(key, field.getFieldName(), field);
    }

    @Override
    public List<FieldVO> listFields(Long modelId) {
        String key = prefix + "field_" + modelId;
        List<FieldVO> list = redisService.hvals(key);
        return list;
    }

    @Override
    public void saveAbstraction(AbstractionVO abstraction) {
        String key = prefix + "abstraction_" + abstraction.getModelId();
        redisService.hset(key, abstraction.getName(), abstraction);
    }

    @Override
    public List<AbstractionVO> listAbstraction(Long modelId) {
        String key = prefix + "abstraction_" + modelId;
        List<AbstractionVO> list = redisService.hget(key, AbstractionVO.class);
        return list;
    }

    @Override
    public void saveActivation(ActivationVO activation) {
        String key = prefix + "activation_" + activation.getModelId();
        redisService.hset(key, activation.getActivationName(), activation);
    }

    @Override
    public List<ActivationVO> listActivation(Long modelId) {
        String key = prefix + "activation_" + modelId;
        List<ActivationVO> list = redisService.hget(key, ActivationVO.class);
        return list;
    }

    @Override
    public void saveAntiFraudResult(String modelId, String sessionId, CommonResult result) {
        String key = prefix + "engine_" + modelId + sessionId;
        redisService.setex(key, result, 5 * 60);
    }

    @Override
    public CommonResult getAntiFraudResult(String modelId, String sessionId) {
        String key = prefix + "engine_" + modelId + sessionId;
        CommonResult result = (CommonResult) redisService.get(key);
        return result;
    }

    @Override
    public void publishModel(ModelVO model) {
        redisService.publish(PUB_SUB_MODEL_CHANNEL, JSON.toJSONString(model));
    }

    @Override
    public void subscribeModel(SubscribeHandle handler) {
        redisService.subscribe(PUB_SUB_MODEL_CHANNEL, handler);

    }

    @Override
    public void publishField(FieldVO field) {
        redisService.publish(PUB_SUB_FIELD_CHANNEL, JSON.toJSONString(field));

    }

    @Override
    public void subscribeField(SubscribeHandle handler) {
        redisService.subscribe(PUB_SUB_FIELD_CHANNEL, handler);

    }

    @Override
    public void publishPreItem(PreItemVO preItem) {
        redisService.publish(PUB_SUB_PREITEM_CHANNEL, JSON.toJSONString(preItem));

    }

    @Override
    public void subscribePreItem(SubscribeHandle handler) {
        redisService.subscribe(PUB_SUB_PREITEM_CHANNEL, handler);

    }

    @Override
    public void publishAbstraction(AbstractionVO model) {
        redisService.publish(PUB_SUB_ABSTRACTION_CHANNEL, JSON.toJSONString(model));

    }

    @Override
    public void subscribeAbstraction(SubscribeHandle handler) {
        redisService.subscribe(PUB_SUB_ABSTRACTION_CHANNEL, handler);

    }

    @Override
    public void publishRule(RuleVO rule) {
        redisService.publish(PUB_SUB_RULE_CHANNEL, JSON.toJSONString(rule));

    }

    @Override
    public void subscribeRule(SubscribeHandle handler) {
        redisService.subscribe(PUB_SUB_RULE_CHANNEL, handler);

    }

    @Override
    public void publishActivation(ActivationVO activation) {
        redisService.publish(PUB_SUB_ACTIVATION_CHANNEL, JSON.toJSONString(activation));

    }

    @Override
    public void subscribeActivation(SubscribeHandle handler) {
        redisService.subscribe(PUB_SUB_ACTIVATION_CHANNEL, handler);

    }

    @Override
    public void publishDataListRecord(DataListRecordVO record) {
        redisService.publish(PUB_SUB_LISTRECORD_CHANNEL, JSON.toJSONString(record));
        
    }

    @Override
    public void subscribeDataListRecord(SubscribeHandle handler) {
        redisService.subscribe(PUB_SUB_LISTRECORD_CHANNEL, handler);
    }

    @Override
    public void publishDataList(DataListsVO dataList) {
        redisService.publish(PUB_SUB_DATALIST_CHANNEL, JSON.toJSONString(dataList));
        
    }

    @Override
    public void subscribeDataList(SubscribeHandle handler) {
        redisService.subscribe(PUB_SUB_DATALIST_CHANNEL, handler);
    }

}
