package com.pgmmers.radar.service.impl.engine;


import com.pgmmers.radar.enums.CombineType;
import com.pgmmers.radar.enums.PluginType;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.AntiFraudEngine;
import com.pgmmers.radar.service.engine.AntiFraudService;
import com.pgmmers.radar.service.engine.PluginService;
import com.pgmmers.radar.service.engine.vo.AbstractionResult;
import com.pgmmers.radar.service.engine.vo.ActivationResult;
import com.pgmmers.radar.service.engine.vo.AdaptationResult;
import com.pgmmers.radar.service.engine.vo.AntiFraudProcessResult;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.service.model.PreItemService;
import com.pgmmers.radar.vo.model.PreItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@ConditionalOnProperty(prefix = "sys.conf", name="app", havingValue = "engine")
public class AntiFraudServiceImpl implements AntiFraudService {

    private static Logger logger = LoggerFactory.getLogger(AntiFraudServiceImpl.class);

    @Autowired
    private AntiFraudEngine antiFraudEngine;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private PreItemService preItemService;
    
    @Autowired
    private ModelService modelService;

    @Override
    public CommonResult process(Long modelId, Map<String, Map<String, ?>> context) {
        AntiFraudProcessResult analysisResult = new AntiFraudProcessResult();
        //上下文参数,用于存放中间计算结果
        long start = 0;
        long end = 0;

        // do abstraction
        logger.info("start abstraction...");
        start = System.currentTimeMillis();
        AbstractionResult absResult = antiFraudEngine.executeAbstraction(modelId, context);
        end = System.currentTimeMillis();
        analysisResult.getRespTimes().put("abstractions", end - start);
        logger.info("takes:{}ms", end - start);
        if (!absResult.isSuccess()) {
            analysisResult.setMsg("abstraction 异常:" + absResult.getMsg());
            return analysisResult;
        } else {
            analysisResult.setAbstractions(context.get("abstractions"));
        }

        // do adaptation
        logger.info("start adaptation...");
        start = System.currentTimeMillis();
        AdaptationResult adaptResult = antiFraudEngine.executeAdaptation(modelId, context);
        end = System.currentTimeMillis();
        analysisResult.getRespTimes().put("adaptations", end - start);
        logger.info("takes:{}ms", end - start);
        if (!adaptResult.isSuccess()) {
            analysisResult.setMsg("adaptation 异常");
            return analysisResult;
        } else {
            analysisResult.setAdaptations(context.get("adaptations"));
        }

        // do activation
        logger.info("start activation...");
        start = System.currentTimeMillis();
        ActivationResult actResult = antiFraudEngine.executeActivation(modelId, context);
        end = System.currentTimeMillis();
        analysisResult.getRespTimes().put("activations", end - start);
        logger.info("takes:{}ms", end - start);
        if (!actResult.isSuccess()) {
            analysisResult.setMsg("Activation 异常");
            return analysisResult;
        } else {
            analysisResult.setActivations(context.get("activations"));
            analysisResult.setHitsDetail(actResult.getHitRulesMap());
        }
        // result 与 context 里面的hitDetail 不一样，主要是为了方便 elastic 处理
        context.put("hitsDetail", actResult.getHitRulesMap2());
        analysisResult.setSuccess(true);
        return analysisResult;
    }

    @Override
    public Map<String, Object> prepare(Long modelId, Map<String, Object> jsonInfo) {
        List<PreItemVO> preItemList = preItemService.listPreItem(modelId);
        Map<String, Object> result = new HashMap<>();
        for (PreItemVO item : preItemList) {
            if (!item.getStatus().equals(StatusType.ACTIVE.getKey())) {
                continue;
            }
            String[] sourceField = item.getSourceField().split(",");
            PluginType plugin = Enum.valueOf(PluginType.class, item.getPlugin());
            Object transfer = "";
            switch (plugin) {
            case IP2LOCATION:
                transfer = pluginService.ip2location(jsonInfo.get(sourceField[0]).toString());
                break;
            case GPS2LOCATION:
                transfer = pluginService.gps2location(jsonInfo.get(sourceField[0]).toString(),
                        jsonInfo.get(sourceField[1]).toString());
                break;
            case ALLINONE:
                List<Object> values = new ArrayList<>();
                for (String field : sourceField) {
                    values.add(jsonInfo.get(field));
                }
                transfer = pluginService.allInOne(values, CombineType.CONCAT);
                break;
            case SUBSTRING:
                String[] args = item.getArgs().split(",");
                transfer = pluginService.subString(jsonInfo.get(sourceField[0]).toString(), Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                break;
            case MOBILE2LOCATION:
                transfer = pluginService.mobile2location(jsonInfo.get(sourceField[0]).toString());
                break;
            case SENSITIVE_TIME:
                Long millis = Long.parseLong(jsonInfo.get(sourceField[0]).toString());
                transfer = pluginService.getSensitiveTime(millis);
                break;
            case DATEFORMAT:
                String  formatStr = item.getArgs();
                millis = Long.parseLong(jsonInfo.get(sourceField[0]).toString());
                transfer = pluginService.formatDate(millis, formatStr);
                break;
            case HTTP_UTIL:
                String  url = item.getArgs();
                String  reqType = item.getReqType();
                String arg = jsonInfo.get(sourceField[0]).toString();
                transfer = pluginService.httpRequest(url, reqType, arg);
                break;
            default:

            }
            result.put(item.getDestField(), transfer);
        }
        return result;
    }

}
