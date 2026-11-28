package com.pgmmers.radar.service.impl.engine;


import com.pgmmers.radar.config.EngineMetrics;
import com.pgmmers.radar.enums.FeatureStage;
import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.AntiFraudEngine;
import com.pgmmers.radar.service.engine.AntiFraudService;
import com.pgmmers.radar.service.engine.feature.FeatureExecutor;
import com.pgmmers.radar.service.engine.feature.FeatureOutcome;
import com.pgmmers.radar.service.engine.vo.FeatureResult;
import com.pgmmers.radar.service.engine.vo.StrategyResult;
import com.pgmmers.radar.service.engine.vo.AdaptationResult;
import com.pgmmers.radar.service.engine.vo.AntiFraudProcessResult;
import com.pgmmers.radar.service.impl.engine.feature.FeatureExecutorRegistry;
import com.pgmmers.radar.service.model.FeatureService;
import com.pgmmers.radar.service.model.ScenarioService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 反欺诈主要服务,仅在引擎端启动的时候才需要加载。
 * @author feihu.wang
 */
@Service
public class AntiFraudServiceImpl implements AntiFraudService {

    private static Logger logger = LoggerFactory.getLogger(AntiFraudServiceImpl.class);

    @Autowired
    private AntiFraudEngine antiFraudEngine;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private FeatureExecutorRegistry executorRegistry;

    @Autowired
    private EngineMetrics engineMetrics;

    @Override
    public CommonResult process(Long scenarioId, Map<String, Map<String, ?>> context) {
        AntiFraudProcessResult analysisResult = new AntiFraudProcessResult();
        //上下文参数,用于存放中间计算结果
        long start = 0;
        long end = 0;

        // do features（ANALYSIS 阶段：AGGREGATE/RATIO）
        logger.info("start features(analysis)...");
        start = System.currentTimeMillis();
        FeatureResult featResult = antiFraudEngine.executeFeatures(scenarioId, context, FeatureStage.ANALYSIS);
        end = System.currentTimeMillis();
        analysisResult.getRespTimes().put("features", end - start);
        engineMetrics.recordStage("features", end - start);
        logger.info("takes:{}ms", end - start);
        if (!featResult.isSuccess()) {
            analysisResult.setMsg("feature 异常:" + featResult.getMsg());
            return analysisResult;
        } else {
            analysisResult.setFeatures(context.get("features"));
        }

        // do adaptation
        logger.info("start adaptation...");
        start = System.currentTimeMillis();
        AdaptationResult adaptResult = antiFraudEngine.executeAdaptation(scenarioId, context);
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

        // do strategy
        logger.info("start strategy...");
        start = System.currentTimeMillis();
        StrategyResult strategyResult = antiFraudEngine.executeStrategy(scenarioId, context);
        end = System.currentTimeMillis();
        analysisResult.getRespTimes().put("strategies", end - start);
        engineMetrics.recordStage("strategies", end - start);
        logger.info("takes:{}ms", end - start);
        if (!strategyResult.isSuccess()) {
            analysisResult.setMsg("Strategy 异常");
            return analysisResult;
        } else {
            // v4.1：引擎响应/ES 文档键统一为 strategies（修复 v4.0 写 activations 与报表读 strategies 的失配）
            analysisResult.setStrategies(context.get("strategies"));
            analysisResult.setHitsDetail(strategyResult.getHitRulesMap());
        }
        // result 与 context 里面的hitDetail 不一样，主要是为了方便 elastic 处理
        context.put("hitsDetail", strategyResult.getHitRulesMap2());
        analysisResult.setSuccess(true);
        return analysisResult;
    }

    /**
     * 入库前特征执行（v4.1 特征工程）：ENTRY 阶段（ENRICH/DERIVED）按 ORDER_NO 顺序分发执行，
     * 产出合并进事件特征集合随实体落库。失败隔离由各执行器/编排层按特征粒度处理。
     */
    @Override
    public Map<String, Object> executeEntryFeatures(Long scenarioId, Map<String, Object> jsonInfo) {
        List<FeatureVO> features = new ArrayList<>();
        for (FeatureVO feature : featureService.listFeature(scenarioId)) {
            if (FeatureStage.ENTRY.getTypes().stream()
                    .anyMatch(t -> t.name().equals(feature.getType()))) {
                features.add(feature);
            }
        }
        features.sort(Comparator.comparingInt(f -> f.getOrderNo() == null ? Integer.MAX_VALUE : f.getOrderNo()));

        // result 即上下文 features 命名空间的同一引用：前序特征产出对后续特征可见（链式）
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, ?>> context = new HashMap<>();
        context.put("fields", jsonInfo);
        context.put("features", result);

        for (FeatureVO feature : features) {
            if (!feature.getStatus().equals(StatusType.ACTIVE.getKey())) {
                continue;
            }
            FeatureType type = FeatureType.of(feature.getType());
            FeatureExecutor executor = type == null ? null : executorRegistry.get(type);
            if (executor == null) {
                logger.warn("feature type {} has no executor, skip feature {} (id={})",
                        type, feature.getFeatureName(), feature.getId());
                engineMetrics.recordFeatureExecution(String.valueOf(type), "no_executor");
                continue;
            }
            try {
                FeatureOutcome outcome = executor.execute(feature, context, Map.of());
                if (outcome.isPresent()) {
                    result.put(feature.getFeatureName(), outcome.getValue());
                }
                engineMetrics.recordFeatureExecution(type.name(), "success");
            } catch (com.pgmmers.radar.service.engine.feature.FeatureIsolationException e) {
                // strict 隔离模式：失败即事件失败（沿袭旧行为），不做单特征吞并
                throw e;
            } catch (Exception e) {
                // 单特征异常隔离（跳过该特征，不影响事件链路）
                logger.warn("entry feature failed, skip feature {} (id={})", feature.getFeatureName(), feature.getId(), e);
                engineMetrics.recordFeatureExecution(type.name(), "error");
            }
        }
        return result;
    }

}
