package com.pgmmers.radar.service.impl.engine;


import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.AntiFraudEngine;
import com.pgmmers.radar.service.engine.AntiFraudService;
import com.pgmmers.radar.service.engine.PluginService;
import com.pgmmers.radar.service.engine.vo.AbstractionResult;
import com.pgmmers.radar.service.engine.vo.ActivationResult;
import com.pgmmers.radar.service.engine.vo.AdaptationResult;
import com.pgmmers.radar.service.engine.vo.AntiFraudProcessResult;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.service.model.PreItemService;
import com.pgmmers.radar.vo.model.PreItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反欺诈主要服务,仅在引擎端启动的时候才需要加载。
 * @author feihu.wang
 */
@Service
@ConditionalOnProperty(prefix = "sys.conf", name="app", havingValue = "engine")
public class AntiFraudServiceImpl implements AntiFraudService {

    private static Logger logger = LoggerFactory.getLogger(AntiFraudServiceImpl.class);

    @Autowired
    private AntiFraudEngine antiFraudEngine;

    @Autowired(required = false)
    private PluginService pluginService;

    @Autowired
    private PreItemService preItemService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private PluginManager pluginManager;

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
            Object transfer = pluginManager.pluginServiceMap(item.getPlugin()).handle(item,jsonInfo,sourceField);
            result.put(item.getDestField(), transfer);
        }
        return result;
    }

}
