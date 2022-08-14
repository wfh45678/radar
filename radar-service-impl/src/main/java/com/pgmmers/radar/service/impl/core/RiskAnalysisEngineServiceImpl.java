package com.pgmmers.radar.service.impl.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.service.RiskAnalysisEngineService;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.data.RiskResultService;
import com.pgmmers.radar.service.engine.AntiFraudService;
import com.pgmmers.radar.service.engine.ValidateService;
import com.pgmmers.radar.service.model.EntityService;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.util.DateUtils;
import com.pgmmers.radar.vo.model.ModelVO;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(prefix = "sys.conf", name = "app", havingValue = "engine")
@Service
public class RiskAnalysisEngineServiceImpl implements RiskAnalysisEngineService {

    private static Logger logger = LoggerFactory.getLogger(RiskAnalysisEngineServiceImpl.class);

    private final ModelService modelService;

    private final EntityService entityService;

    private final AntiFraudService antiFraudService;

    @Value("${sys.conf.entity-duplicate-insert}")
    private String isDuplicate;

    private final CacheService cacheService;

    private final  RiskResultService riskResultService;

    private final ValidateService validateService;

    public RiskAnalysisEngineServiceImpl(
            ModelService modelService, EntityService entityService,
            AntiFraudService antiFraudService, CacheService cacheService,
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") RiskResultService riskResultService,
            ValidateService validateService) {
        this.modelService = modelService;
        this.entityService = entityService;
        this.antiFraudService = antiFraudService;
        this.cacheService = cacheService;
        this.riskResultService = riskResultService;
        this.validateService = validateService;
    }

    @Override
    public CommonResult uploadInfo(String modelGuid, String reqId, String jsonInfo) {
        return uploadInfo(modelGuid, reqId, JSON.parseObject(jsonInfo));
    }

    @Override
    public CommonResult uploadInfo(String modelGuid, String reqId, JSONObject jsonInfo) {
        logger.info("req info:{},{},{}", modelGuid, reqId, jsonInfo);
        CommonResult result = new CommonResult();
        Map<String, Map<String, ?>> context = new HashMap<>();
        ModelVO model;
        try {
            // 1. check
            JSONObject eventJson = jsonInfo;

            model = modelService.getModelByGuid(modelGuid);

            if (model == null) {
                result.setMsg("模型不存在!");
                return result;
            }

            if (model.getStatus() != StatusType.ACTIVE.getKey()) {
                result.setMsg("模型未激活");
                return result;
            }

            if (model.getFieldValidate()) {
                Map<String, Object> vldMap = validateService.validate(model.getId(), eventJson);
                if (vldMap.size() > 0) {
                    result.setData(vldMap);
                    result.setMsg("参数校验不通过");
                    return result;
                }
            }

            // 2. pre process
            Map<String, Object> preItemMap = antiFraudService.prepare(model.getId(), eventJson);

            // 3. save to db
            boolean isAllowDuplicate = false;
            if (isDuplicate != null && "true".equals(isDuplicate)) {
                isAllowDuplicate = true;
            }
            entityService
                    .save(model.getId(), eventJson.toJSONString(), JSON.toJSONString(preItemMap),
                            isAllowDuplicate);

            // 4. 执行分析
            context.put("fields", eventJson);
            context.put("preItems", preItemMap);
            result = antiFraudService.process(model.getId(), context);

            // 5. for elastic analysis
            Long eventTimeMillis = (Long) eventJson.get(model.getReferenceDate());
            String timeStr = DateUtils
                    .formatDate(new Date(eventTimeMillis), "yyyy-MM-dd'T'HH:mm:ssZ");
            preItemMap.put("radar_ref_datetime", timeStr);
        } catch (Exception e) {
            logger.error("process error", e);
            //result.setMsg("数据异常!" + e.getMessage());
            throw new RuntimeException("数据处理异常:" + e.getMessage());
        }

        // 缓存分析结果
        cacheService.saveAntiFraudResult(modelGuid, reqId, result);

        // 保存事件信息和分析结果用于后续分析
        riskResultService.sendResult(modelGuid, reqId, JSON.toJSONString(context));
        // 返回分析结果
        return result;
    }


    @Override
    public CommonResult getScore(String modelGuid, String reqId) {
        CommonResult result = cacheService.getAntiFraudResult(modelGuid, reqId);
        if (result == null) {
            result = new CommonResult();
            result.setCode("601");
            result.setMsg("reqId 已经过期.");
        }
        return result;
    }

    @Override
    public CommonResult syncStatus(String modelGuid, String eventId, String status) {
        CommonResult result = new CommonResult();
        ModelVO model = modelService.getModelByGuid(modelGuid);
        if (model == null) {
            result.setMsg("模型不存在!");
            return result;
        }
        long cnt = entityService.update(model.getId(), eventId, status);
        result.setSuccess(cnt > 0 ? true : false);
        return result;
    }


}
