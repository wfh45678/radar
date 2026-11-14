package com.pgmmers.radar.service.impl.engine;


import com.pgmmers.radar.config.EngineMetrics;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.AntiFraudEngine;
import com.pgmmers.radar.service.engine.AntiFraudService;
import com.pgmmers.radar.service.engine.vo.AbstractionResult;
import com.pgmmers.radar.service.engine.vo.ActivationResult;
import com.pgmmers.radar.service.engine.vo.AdaptationResult;
import com.pgmmers.radar.service.engine.vo.AntiFraudProcessResult;
import com.pgmmers.radar.service.impl.engine.plugin.PluginEntry;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.service.model.PreItemService;
import com.pgmmers.radar.model.vo.model.PreItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反欺诈主要服务,仅在引擎端启动的时候才需要加载。
 * @author feihu.wang
 */
@Service
public class AntiFraudServiceImpl implements AntiFraudService {

    private static Logger logger = LoggerFactory.getLogger(AntiFraudServiceImpl.class);

    private static final String STRICT = "strict";

    /** 插件失败隔离模式：isolated（默认，失败标记后继续） / strict（旧行为，失败即事件失败）。 */
    @Value("${plugin.isolation:isolated}")
    private String isolation;

    @Autowired
    private AntiFraudEngine antiFraudEngine;

    @Autowired
    private PreItemService preItemService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private EngineMetrics engineMetrics;

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
        engineMetrics.recordStage("abstractions", end - start);
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
        engineMetrics.recordStage("adaptations", end - start);
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
        engineMetrics.recordStage("activations", end - start);
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

    /**
     * preItems 阶段（v3.0 插件加固）：逐个插件执行，默认失败隔离——
     * 单个插件异常/配置错误只记指标并写错误标记，事件链路继续；
     * {@code plugin.isolation=strict} 时保留旧行为（失败即事件失败）。
     */
    @Override
    public Map<String, Object> prepare(Long modelId, Map<String, Object> jsonInfo) {
        List<PreItemVO> preItemList = preItemService.listPreItem(modelId);
        Map<String, Object> result = new HashMap<>();
        for (PreItemVO item : preItemList) {
            if (!item.getStatus().equals(StatusType.ACTIVE.getKey())) {
                continue;
            }
            String[] sourceField = item.getSourceField().split(",");
            String pluginKey = item.getPlugin();
            PluginEntry entry = pluginManager.entry(pluginKey);
            long start = System.currentTimeMillis();
            Object transfer = null;
            String failReason = null;
            if (entry == null) {
                failReason = "plugin not found: " + pluginKey;
                logger.warn("preItem plugin missing, modelId={}, destField={}, {}", modelId, item.getDestField(), failReason);
            } else {
                try {
                    transfer = entry.execute(item, jsonInfo, sourceField);
                } catch (Throwable t) {
                    failReason = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
                    logger.warn("preItem plugin failed: {}, destField={}, reason={}", pluginKey, item.getDestField(), failReason);
                }
            }
            long elapsed = System.currentTimeMillis() - start;
            String metricResult = failReason == null ? "success"
                    : (entry == null ? "not_found" : "fail");
            engineMetrics.recordPluginExecution(entry == null ? "unknown" : pluginKey, metricResult, elapsed);
            if (failReason != null) {
                if (STRICT.equals(isolation)) {
                    throw new RuntimeException("preItem plugin fail: " + pluginKey + " - " + failReason);
                }
                Map<String, Object> errorMarker = new HashMap<>();
                errorMarker.put("__plugin_error__", failReason);
                transfer = errorMarker;
            }
            result.put(item.getDestField(), transfer);
        }
        return result;
    }

}
