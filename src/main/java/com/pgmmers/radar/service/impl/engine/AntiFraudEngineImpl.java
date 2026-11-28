package com.pgmmers.radar.service.impl.engine;


import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.enums.FeatureStage;
import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.config.EngineMetrics;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.enums.StrategyType;
import com.pgmmers.radar.service.dnn.Estimator;
import com.pgmmers.radar.service.engine.AntiFraudEngine;
import com.pgmmers.radar.service.engine.feature.FeatureExecutor;
import com.pgmmers.radar.service.engine.feature.FeatureOutcome;
import com.pgmmers.radar.service.engine.strategy.StrategyExecutor;
import com.pgmmers.radar.service.engine.strategy.StrategyOutcome;
import com.pgmmers.radar.service.engine.vo.FeatureResult;
import com.pgmmers.radar.service.engine.vo.StrategyResult;
import com.pgmmers.radar.service.engine.vo.AdaptationResult;
import com.pgmmers.radar.service.engine.vo.HitObject;
import com.pgmmers.radar.service.engine.vo.RiskObject;
import com.pgmmers.radar.service.impl.dnn.EstimatorContainer;
import com.pgmmers.radar.service.impl.engine.feature.FeatureExecutorRegistry;
import com.pgmmers.radar.service.impl.engine.strategy.StrategyExecutorRegistry;
import com.pgmmers.radar.service.impl.util.JsonParserUtil;
import com.pgmmers.radar.service.model.FeatureService;
import com.pgmmers.radar.service.model.StrategyService;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.service.model.EntityService;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.service.model.ScenarioService;
import com.pgmmers.radar.common.util.DateUtils;
import com.pgmmers.radar.service.engine.strategy.ConditionEvaluator;
import com.pgmmers.radar.common.util.GroovyScriptUtil;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.model.vo.model.DataListRecordVO;
import com.pgmmers.radar.model.vo.model.DataListsVO;
import com.pgmmers.radar.model.vo.model.FieldVO;
import com.pgmmers.radar.model.vo.model.ScenarioVO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AntiFraudEngineImpl implements AntiFraudEngine {
    private static Logger logger = LoggerFactory.getLogger(AntiFraudEngineImpl.class);

    private static Map<Long, Map<String, Object>> dataListCacheMap = new HashMap<>();
    @Value("${sys.conf.machine-learning: false}")
    private boolean machineLearning;
    @Autowired
    private EntityService entityService;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private FeatureExecutorRegistry featureExecutorRegistry;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private DataListsService dataListsService;

    @Autowired
    private StrategyExecutorRegistry executorRegistry;

    @Autowired
    private EstimatorContainer estimatorContainer;

    @Autowired
    private EngineMetrics engineMetrics;

    @Override
    public FeatureResult executeFeatures(Long scenarioId, Map<String, Map<String, ?>> data, FeatureStage stage) {
        FeatureResult result = new FeatureResult();
        List<FeatureVO> features = new ArrayList<>();
        for (FeatureVO feature : featureService.listFeature(scenarioId)) {
            if (stage.getTypes().stream().anyMatch(t -> t.name().equals(feature.getType()))) {
                features.add(feature);
            }
        }
        features.sort(Comparator.comparingInt(f -> f.getOrderNo() == null ? Integer.MAX_VALUE : f.getOrderNo()));

        Map<String, Object> dataCollectionMap = dataListsService.getDataListMap(scenarioId);
        for (FeatureVO feature : features) {
            if (!feature.getStatus().equals(StatusType.ACTIVE.getKey())) {
                continue;
            }
            FeatureType type = FeatureType.of(feature.getType());
            FeatureExecutor executor = type == null ? null : featureExecutorRegistry.get(type);
            if (executor == null) {
                logger.warn("feature type {} has no executor, skip feature {} (id={})",
                        type, feature.getFeatureName(), feature.getId());
                engineMetrics.recordFeatureExecution(String.valueOf(type), "no_executor");
                continue;
            }
            try {
                FeatureOutcome outcome = executor.execute(feature, data, dataCollectionMap);
                if (outcome.isPresent()) {
                    result.getFeatureMap().put(feature.getFeatureName(), outcome.getValue());
                }
            } catch (Exception e) {
                // 单特征异常隔离（跳过该特征，不影响事件链路）
                logger.warn("feature execute error, skip feature {} (id={})", feature.getFeatureName(), feature.getId(), e);
                engineMetrics.recordFeatureExecution(type.name(), "error");
            }
        }
        mergeFeatures(data, result.getFeatureMap());
        result.setSuccess(true);
        return result;
    }

    /** 把本阶段特征产出合并进 context 的 features 命名空间（不覆盖 ENRICH/DERIVED 已产出项以外的键）。 */
    @SuppressWarnings("unchecked")
    private void mergeFeatures(Map<String, Map<String, ?>> data, Map<String, Object> featureMap) {
        Map<String, Object> features = (Map<String, Object>) data.get("features");
        if (features == null) {
            features = new HashMap<>();
            data.put("features", features);
        }
        features.putAll(featureMap);
    }

    /**
     *
     * 后续需要优化.delete from next version
     *
     * @param scenarioId
     * @return
     * @author feihu.wang
     * 2016年8月10日
     */
    @Deprecated
    private Map<String, Object> getPrepareDataCollection(Long scenarioId) {
        Map<String, Object> dataListMap = null;
        if (dataListCacheMap.containsKey(scenarioId)) {
            dataListMap = dataListCacheMap.get(scenarioId);
            return dataListMap;
        }
        dataListMap = new HashMap<>();
        List<DataListsVO> list = dataListsService.listDataLists(scenarioId, StatusType.ACTIVE.getKey());
        // 系统自带黑/白名单
        List<DataListsVO> list2 = dataListsService.listDataLists(1L, StatusType.ACTIVE.getKey());
        list.addAll(list2);

        Map<String, String> dataListRecords;
        for (DataListsVO vo : list) {
            Long dataListId = vo.getId();
            List<DataListRecordVO> records = dataListsService.listDataListRecords(dataListId);
            dataListRecords = new HashMap<>();
            for (DataListRecordVO record : records) {
                dataListRecords.put(record.getDataRecord(), "");
            }
            dataListMap.put(vo.getName(), dataListRecords);
        }
        dataListCacheMap.put(scenarioId, dataListMap);
        return dataListMap;
    }

    @Override
    public AdaptationResult executeAdaptation(Long scenarioId, Map<String, Map<String, ?>> data) {
        AdaptationResult result = new AdaptationResult();
//      启动机器学习
        if (machineLearning){
            Estimator estimator = estimatorContainer.getByScenarioId(scenarioId);
            if(estimator != null) {
                float score = estimator.predict(scenarioId, data);
                result.getAdaptationMap().put("score", score);
            }
        }
        result.setSuccess(true);
        data.put("adaptations", result.getAdaptationMap());
        return result;
    }

    @Override
    public StrategyResult executeStrategy(Long scenarioId, Map<String, Map<String, ?>> data) {
        StrategyResult result = new StrategyResult();
        List<StrategyVO> strategies = strategyService.listStrategy(scenarioId);
        // 获取预加载的黑/白名单集合
        Map<String, Object> dataCollectionMap = dataListsService.getDataListMap(scenarioId);

        for (StrategyVO strategy : strategies) {
            if (!strategy.getStatus().equals(StatusType.ACTIVE.getKey())) {
                continue;
            }
            String name = strategy.getStrategyName();
            result.getHitRulesMap().put(name, new ArrayList<>());
            result.getHitRulesMap2().put(name, new HashMap<>());
            // 按 type 分发到策略执行器；单策略异常隔离（跳过该策略，不影响其他策略与事件成功）
            StrategyOutcome outcome;
            try {
                StrategyType type = StrategyType.of(strategy.getType());
                StrategyExecutor executor = executorRegistry.get(type);
                if (executor == null) {
                    logger.warn("strategy type {} has no executor, skip strategy {} (id={})", type, name, strategy.getId());
                    engineMetrics.recordStrategyExecution(type.name(), "no_executor");
                    continue;
                }
                outcome = executor.execute(strategy, data, dataCollectionMap);
            } catch (Exception e) {
                logger.error("strategy execute error, skip strategy {} (id={})", name, strategy.getId(), e);
                engineMetrics.recordStrategyExecution("unknown", "error");
                continue;
            }
            result.getHitRulesMap().put(name, outcome.getHits());
            result.getHitRulesMap2().put(name, outcome.getHits2());
            engineMetrics.recordStrategyExecution(StrategyType.of(strategy.getType()).name(), "success");
            engineMetrics.recordDecision(outcome.getRisk());
            RiskObject riskObj = new RiskObject();
            riskObj.setRisk(outcome.getRisk());
            riskObj.setScore(outcome.getScore().intValue());
            result.getStrategyMap().put(name, riskObj);
        }
        // v4.1：上下文键统一 strategies（修复 v4.0 写 activations 与报表/导出读 strategies 的失配）
        data.put("strategies", result.getStrategyMap());
        result.setSuccess(true);
        return result;
    }

}
