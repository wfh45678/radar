package com.pgmmers.radar.service.impl.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.service.RiskAnalysisEngineService;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.AntiFraudService;
import com.pgmmers.radar.service.engine.ValidateService;
import com.pgmmers.radar.service.model.EntityService;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.util.DateUtils;
import com.pgmmers.radar.vo.model.ModelVO;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(prefix = "sys.conf", name="app", havingValue = "engine")
@Service
public class RiskAnalysisEngineServiceImpl implements RiskAnalysisEngineService {
    private static Logger logger = LoggerFactory.getLogger(RiskAnalysisEngineServiceImpl.class);

    @Autowired
    private ModelService modelService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private AntiFraudService antiFraudService;

    @Value("${sys.conf.entity-duplicate-insert}")
    private String isDuplicate;

    @Autowired
    private CacheService cacheService;


    @Autowired
    private ValidateService validateService;


    @Autowired
    private RestHighLevelClient esClient;

    @Override
    public CommonResult uploadInfo(String modelGuid, String reqId, String jsonInfo) {
        logger.info("req info:{},{},{}", modelGuid, reqId, jsonInfo);
        CommonResult result = new CommonResult();
        Map<String, Map<String, ?>> context = new HashMap<>();
        ModelVO model;
        try {
            // 1. check
            JSONObject eventJson = JSON.parseObject(jsonInfo);

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
            if (isDuplicate != null && isDuplicate.equals("true")) {
                isAllowDuplicate = true;
            }
            entityService.save(model.getId(), eventJson.toJSONString(), JSON.toJSONString(preItemMap), isAllowDuplicate);

            // 4. 执行分析
            context.put("fields", eventJson);
            context.put("preItems", preItemMap);
            result = antiFraudService.process(model.getId(), context);
            
            // 5. for elastic analysis
            Long eventTimeMillis = (Long) eventJson.get(model.getReferenceDate());
            String timeStr = DateUtils.formatDate(new Date(eventTimeMillis), "yyyy-MM-dd'T'HH:mm:ssZ");
            preItemMap.put("radar_ref_datetime", timeStr);
        } catch (Exception e) {
            e.printStackTrace();
            result.setMsg("数据异常!");
        }

        // 缓存分析结果
        cacheService.saveAntiFraudResult(modelGuid, reqId, result);

        // 保存事件信息和分析结果用于后续分析
        try {
            sendResult(modelGuid, reqId, JSON.toJSONString(context));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            logger.error("向es中保存数据失败！");
        }
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

    /**
     * 通过消息队列缓存事件内容和分析结果，用于实时或者离线分析.
     * @param modelGuid
     * @param reqId
     * @param info event info and analyze result.
     */
    private void sendResult(String modelGuid, String reqId, String info) throws IOException {
        // 这里可以根据情况进行异步处理。
        send2ES(modelGuid, reqId, info);
    }

    private void send2ES(String guid, String reqId, String json) throws IOException {
        IndexRequest request = new IndexRequest(guid.toLowerCase());
        request.id(reqId);
        request.source(json, XContentType.JSON);
        IndexResponse result = this.esClient.index(request, RequestOptions.DEFAULT);
//        ResponseEntity<String> result = restTemplate.postForEntity(url, requestEntity, String.class, new Object[]{});
        logger.info("es result:{}", result.toString());
    }


}
