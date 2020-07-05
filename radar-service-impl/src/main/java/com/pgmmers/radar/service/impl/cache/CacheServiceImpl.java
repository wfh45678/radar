package com.pgmmers.radar.service.impl.cache;

import com.alibaba.fastjson.JSON;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.RedisService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.model.AbstractionVO;
import com.pgmmers.radar.vo.model.ActivationVO;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import com.pgmmers.radar.vo.model.DataListsVO;
import com.pgmmers.radar.vo.model.FieldVO;
import com.pgmmers.radar.vo.model.ModelVO;
import com.pgmmers.radar.vo.model.PreItemVO;
import com.pgmmers.radar.vo.model.RuleVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public static final String LOGIN_CAPTCHA_PREFIX = "log_captcha_";


    @Autowired
    private RedisService redisService;

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

    @Override
    public void cacheCaptcha(String captcha) {
        redisService.setex(LOGIN_CAPTCHA_PREFIX + captcha, captcha, 60);
    }

    @Override
    public boolean validateCaptcha(String captcha) {
        return redisService.contains(LOGIN_CAPTCHA_PREFIX + captcha.toUpperCase());
    }

}
