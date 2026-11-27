package com.pgmmers.radar.service.impl.engine;


import com.pgmmers.radar.enums.AggregateType;
import com.pgmmers.radar.config.EngineMetrics;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.enums.StrategyType;
import com.pgmmers.radar.service.dnn.Estimator;
import com.pgmmers.radar.service.engine.AggregateCommand;
import com.pgmmers.radar.service.engine.AntiFraudEngine;
import com.pgmmers.radar.service.engine.strategy.StrategyExecutor;
import com.pgmmers.radar.service.engine.strategy.StrategyOutcome;
import com.pgmmers.radar.service.engine.vo.AbstractionResult;
import com.pgmmers.radar.service.engine.vo.StrategyResult;
import com.pgmmers.radar.service.engine.vo.AdaptationResult;
import com.pgmmers.radar.service.engine.vo.HitObject;
import com.pgmmers.radar.service.engine.vo.RiskObject;
import com.pgmmers.radar.service.impl.dnn.EstimatorContainer;
import com.pgmmers.radar.service.impl.engine.strategy.StrategyExecutorRegistry;
import com.pgmmers.radar.service.impl.util.JsonParserUtil;
import com.pgmmers.radar.service.model.AbstractionService;
import com.pgmmers.radar.service.model.StrategyService;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.service.model.EntityService;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.common.util.DateUtils;
import com.pgmmers.radar.common.util.GroovyScriptUtil;
import com.pgmmers.radar.model.vo.model.AbstractionVO;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.model.vo.model.DataListRecordVO;
import com.pgmmers.radar.model.vo.model.DataListsVO;
import com.pgmmers.radar.model.vo.model.FieldVO;
import com.pgmmers.radar.model.vo.model.ModelVO;
import java.math.BigDecimal;
import java.util.ArrayList;
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
    private AbstractionService abstractionService;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private DataListsService dataListsService;

    @Autowired
    private AggregateCommand aggregateCommand;

    @Autowired
    private StrategyExecutorRegistry executorRegistry;

    @Autowired
    private EstimatorContainer estimatorContainer;

    @Autowired
    private EngineMetrics engineMetrics;

    @Override
    public AbstractionResult executeAbstraction(Long modelId, Map<String, Map<String, ?>> data) {
        AbstractionResult result = new AbstractionResult();
        // 1. 解析 参数信息
        //JSONObject entity = (JSONObject) data.get("entity");

        // 2. list abstraction
        List<AbstractionVO> abstractions = abstractionService.listAbstraction(modelId);

        // 排除没有的定义 abstraction 的情况。
        if (abstractions == null || abstractions.size() == 0) {
            data.put("abstractions", result.getAbstractionMap());
            result.setSuccess(true);
            return result;
        }
        // 3. 按 script 的条件， 分别统计 abstraction
        for (AbstractionVO abs : abstractions) {

            if (!abs.getStatus().equals(StatusType.ACTIVE.getKey())) {
                continue;
            }

            // abs get aggregate type

            String searchField = abs.getSearchField();
            searchField = searchField.replace("fields.", "").replace("preItems.", "");

            Integer aggregateType = abs.getAggregateType();

            Integer dateType = abs.getSearchIntervalType();

            Integer interval = abs.getSearchIntervalValue();

            String funcionField = abs.getFunctionField();
            funcionField = funcionField.replace("fields.", "").replace("preItems.", "");

            FieldType functionFieldType = null;

            String ruleScript = abs.getRuleScript();

            // 获取预加载的黑/白名单集合
            Map<String, Object> dataCollectionMap = dataListsService.getDataListMap(modelId);
            boolean matched = checkAbstractionScript(ruleScript, data, dataCollectionMap);
            if (!matched) {
                // 脚本检查不通过，过滤该 abstraction
                //data.put(abs.getName(), 0);
                result.getAbstractionMap().put(abs.getName(), -1);
                continue;
            }
            ModelVO model = modelService.getModelById(modelId);
            String refDateFieldName = model.getReferenceDate();
            Date refDate;
            try {
                Long refDatetimeMills = (Long) data.get("fields").get(refDateFieldName);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(refDatetimeMills);
                refDate = c.getTime();
            } catch (Exception e) {
                e.printStackTrace();
                result.setMsg("时间不正确");
                return result;
            }
            if (refDate == null) {
                result.setMsg("时间不正确");
                return result;
            }
            Date beginDate = DateUtils.addDate(refDate, dateType, interval * -1).getTime();
            Object calResult = null;
            Object searchFieldVal = data.get("fields").get(searchField);
            if (searchFieldVal == null) {
                searchFieldVal = data.get("preItems").get(searchField);
                if (searchFieldVal == null) {
                    searchFieldVal = JsonParserUtil.value(data.get("preItems"), searchField, null);
                }
            }
            if (searchFieldVal == null) {
                result.setMsg("search field value eq null!");
                return result;
            }

            if (!StringUtils.isEmpty(funcionField)) {
                List<FieldVO> fields = fieldService.listField(modelId);
                if (fields == null || fields.size() == 0) {
                    result.setMsg("no field found!");
                    return result;
                }
                for (FieldVO field : fields) {
                    if (field.getFieldName().equals(funcionField)) {
                        functionFieldType = FieldType.valueOf(field.getFieldType());
                        break;
                    }
                }
                if (functionFieldType == null) {
                    // 因为预处理字段没有字段类型，暂时设置为String
                    // TODO:  目前只有高级函数使用了 functionFieldType。
                    //result.setMsg("function field type is null");
                    //return result;
                    functionFieldType = FieldType.valueOf("STRING");
                }
            }

            switch (aggregateType) {
            case AggregateType.COUNT:
                calResult = aggregateCommand.count(modelId.toString(), searchField, searchFieldVal, refDateFieldName,
                        beginDate, refDate);
                break;
            case AggregateType.DISTINCT_COUNT:
                calResult = aggregateCommand.distinctCount(modelId.toString(), searchField, searchFieldVal,
                        refDateFieldName, beginDate, refDate, funcionField);
                break;
            case AggregateType.SUM:
                calResult = aggregateCommand.sum(modelId.toString(), searchField, searchFieldVal, refDateFieldName,
                        beginDate, refDate, funcionField);
                break;
            case AggregateType.AVERAGE:
                calResult = aggregateCommand.average(modelId.toString(), searchField, searchFieldVal, refDateFieldName,
                        beginDate, refDate, funcionField);
                break;
            case AggregateType.MAX:
                calResult = aggregateCommand.max(modelId.toString(), searchField, searchFieldVal, refDateFieldName,
                        beginDate, refDate, funcionField);
                break;
            case AggregateType.MIN:
                calResult = aggregateCommand.min(modelId.toString(), searchField, searchFieldVal, refDateFieldName,
                        beginDate, refDate, funcionField);
                break;
            case AggregateType.SD:
                calResult = aggregateCommand.sd(modelId.toString(), searchField, searchFieldVal, refDateFieldName,
                        beginDate, refDate, funcionField, functionFieldType);
                break;
            case AggregateType.VARIANCE:
                calResult = aggregateCommand.variance(modelId.toString(), searchField, searchFieldVal,
                        refDateFieldName, beginDate, refDate, funcionField, functionFieldType);
                break;
            case AggregateType.DEVIATION:
                Object val = data.get("fields").get(funcionField);
                calResult = aggregateCommand.deviation(modelId.toString(), searchField, searchFieldVal,
                        refDateFieldName, beginDate, refDate, funcionField, functionFieldType,
                        new BigDecimal(val.toString()));
                break;
            case AggregateType.MEDIAN:
                calResult = aggregateCommand.median(modelId.toString(), searchField, searchFieldVal, refDateFieldName,
                        beginDate, refDate, funcionField);
                break;
            default:

            }
            if (calResult instanceof BigDecimal) {
                result.getAbstractionMap().put(abs.getName(), ((BigDecimal) calResult).doubleValue());
            } else {
                result.getAbstractionMap().put(abs.getName(), calResult);
            }

        }
        data.put("abstractions", result.getAbstractionMap());
        result.setSuccess(true);
        return result;

    }

    /**
     *
     * 后续需要优化.delete from next version
     *
     * @param modelId
     * @return
     * @author feihu.wang
     * 2016年8月10日
     */
    @Deprecated
    private Map<String, Object> getPrepareDataCollection(Long modelId) {
        Map<String, Object> dataListMap = null;
        if (dataListCacheMap.containsKey(modelId)) {
            dataListMap = dataListCacheMap.get(modelId);
            return dataListMap;
        }
        dataListMap = new HashMap<>();
        List<DataListsVO> list = dataListsService.listDataLists(modelId, StatusType.ACTIVE.getKey());
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
        dataListCacheMap.put(modelId, dataListMap);
        return dataListMap;
    }

    @Override
    public AdaptationResult executeAdaptation(Long modelId, Map<String, Map<String, ?>> data) {
        AdaptationResult result = new AdaptationResult();
//      启动机器学习
        if (machineLearning){
            Estimator estimator = estimatorContainer.getByModelId(modelId);
            if(estimator != null) {
                float score = estimator.predict(modelId, data);
                result.getAdaptationMap().put("score", score);
            }
        }
        result.setSuccess(true);
        data.put("adaptations", result.getAdaptationMap());
        return result;
    }

    @Override
    public StrategyResult executeStrategy(Long modelId, Map<String, Map<String, ?>> data) {
        StrategyResult result = new StrategyResult();
        List<StrategyVO> strategies = strategyService.listStrategy(modelId);
        // 获取预加载的黑/白名单集合
        Map<String, Object> dataCollectionMap = dataListsService.getDataListMap(modelId);

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
        // context 键保持 activations（ES 文档与报表检索依赖该键，v4.0 策略更名不改变对外数据形态）
        data.put("activations", result.getStrategyMap());
        result.setSuccess(true);
        return result;
    }

    /**
     *
     * 检查数据是否符合条件.
     *
     * @param ruleScript string
     * @param entity map of params
     * @param dataCollectionMap like black list
     * @return true data is valid, other is false, {@link Boolean}
     * @author feihu.wang
     * 2016年8月2日
     */
    private Boolean checkAbstractionScript(String ruleScript, Map entity, Map<String, Object> dataCollectionMap) {
        Object[] args = { entity, dataCollectionMap };
        Boolean ret = false;
        try {
            ret = (Boolean) GroovyScriptUtil.invokeMethod(ruleScript, "check", args);
        } catch (Exception e) {
            logger.info("rule:{}", ruleScript);
            logger.error("", e);
        }
        return ret;
    }
}
