package com.pgmmers.radar.service.impl.engine;


import com.pgmmers.radar.enums.AggregateType;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.enums.Operator;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.service.dnn.Estimator;
import com.pgmmers.radar.service.engine.AggregateCommand;
import com.pgmmers.radar.service.engine.AntiFraudEngine;
import com.pgmmers.radar.service.engine.vo.AbstractionResult;
import com.pgmmers.radar.service.engine.vo.ActivationResult;
import com.pgmmers.radar.service.engine.vo.AdaptationResult;
import com.pgmmers.radar.service.engine.vo.HitObject;
import com.pgmmers.radar.service.engine.vo.RiskObject;
import com.pgmmers.radar.service.impl.dnn.EstimatorContainer;
import com.pgmmers.radar.service.model.AbstractionService;
import com.pgmmers.radar.service.model.ActivationService;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.service.model.EntityService;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.service.model.RuleService;
import com.pgmmers.radar.util.DateUtils;
import com.pgmmers.radar.util.GroovyScriptUtil;
import com.pgmmers.radar.vo.model.AbstractionVO;
import com.pgmmers.radar.vo.model.ActivationVO;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import com.pgmmers.radar.vo.model.DataListsVO;
import com.pgmmers.radar.vo.model.FieldVO;
import com.pgmmers.radar.vo.model.ModelVO;
import com.pgmmers.radar.vo.model.RuleVO;
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

    private static Map<Long, Map<String, Object>> dataListCacheMap = new HashMap<Long, Map<String, Object>>();
    @Value("${sys.conf.machine-learning: true}")
    private boolean machineLearning;
    @Autowired
    private EntityService entityService;

    @Autowired
    private AbstractionService abstractionService;

    @Autowired
    private ActivationService activationService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private DataListsService dataListsService;

    @Autowired
    private AggregateCommand aggregateCommand;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private EstimatorContainer estimatorContainer;

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
                    result.setMsg("function field type is null");
                    return result;
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
    private Map<String, Object> getPrepareDataCollection(Long modelId) {
        Map<String, Object> dataListMap = null;
        if (dataListCacheMap.containsKey(modelId)) {
            dataListMap = dataListCacheMap.get(modelId);
            return dataListMap;
        }
        dataListMap = new HashMap<>();
        List<DataListsVO> list = dataListsService.listDataLists(modelId, StatusType.ACTIVE.getKey());
        // 系统自带黑/白名单
        List<DataListsVO> list2 = dataListsService.listDataLists(0L, StatusType.ACTIVE.getKey());
        list.addAll(list2);

        Map<String, String> dataListRecords;
        for (DataListsVO vo : list) {
            Long dataListId = vo.getId();
            List<DataListRecordVO> records = dataListsService.listDataListRecords(dataListId);
            dataListRecords = new HashMap<String, String>();
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
    public ActivationResult executeActivation(Long modelId, Map<String, Map<String, ?>> data) {
        ActivationResult result = new ActivationResult();
        List<ActivationVO> activations = activationService.listActivation(modelId);
        // 获取预加载的黑/白名单集合
        Map<String, Object> dataCollectionMap = dataListsService.getDataListMap(modelId);

        for (ActivationVO act : activations) {
            if (!act.getStatus().equals(StatusType.ACTIVE.getKey())) {
                continue;
            }
            result.getHitRulesMap().put(act.getActivationName(), new ArrayList<>());
            result.getHitRulesMap2().put(act.getActivationName(), new HashMap<>());
            // get rule scripts
            List<RuleVO> rules = ruleService.listRule(act.getId());
            BigDecimal sum = BigDecimal.ZERO;
            for (RuleVO rule : rules) {
                if (!rule.getStatus().equals(StatusType.ACTIVE.getKey())) {
                    continue;
                }
                String scripts = rule.getScripts();
                boolean matched = checkActivationScript(scripts, data, dataCollectionMap);
                //规则匹配计算score
                if (matched) {
                    String opt = rule.getOperator();
                    Operator operator = Enum.valueOf(Operator.class, opt);
                    BigDecimal initScore = new BigDecimal(rule.getInitScore());
                    // 后续应该需要增加比例值
                    BigDecimal rate = new BigDecimal(rule.getRate() * 0.01);
                    BigDecimal base = new BigDecimal(rule.getBaseNum());
                    BigDecimal extra = BigDecimal.ZERO;
                    String by = rule.getAbstractionName();
                    if (!StringUtils.isEmpty(by)) {
                        Object abs = data.get("abstractions").get(rule.getAbstractionName());
                        if (abs != null) {
                            extra = new BigDecimal(abs.toString());
                        }
                    }
                    extra = extra.multiply(rate);
                    switch (operator) {
                    case ADD:
                        extra = base.add(extra);
                        break;
                    case SUB:
                        extra = base.subtract(extra);
                        break;
                    case MUL:
                        extra = base.multiply(extra);
                        break;
                    case DIV:
                        extra = base.divide(extra, 2, 4);
                        break;
                    default:
                    }
                    BigDecimal account = initScore.add(extra);
                    HitObject hit = new HitObject();
                    hit.setKey(rule.getId().toString());
                    hit.setDesc(rule.getLabel());
                    hit.setValue(account.setScale(2, 4).doubleValue());
                    result.getHitRulesMap().get(act.getActivationName()).add(hit);
                    result.getHitRulesMap2().get(act.getActivationName()).put("rule_"+ hit.getKey(), hit);
                    sum = sum.add(account);
                }

            }
            sum = sum.setScale(0, 4);
            // 计算风险级别
            BigDecimal high = new BigDecimal(act.getHigh());
            BigDecimal median = new BigDecimal(act.getMedian());
            BigDecimal bottom = new BigDecimal(act.getBottom());
            String risk = "pass";
            if (sum.compareTo(high) >= 0) {
                risk = "reject";
            } else if (sum.compareTo(median) >= 0) {
                risk = "review";
            } else {
                risk = "pass";
            }
            RiskObject riskObj = new RiskObject();
            riskObj.setRisk(risk);
            riskObj.setScore(sum.intValue());
            result.getActivationMap().put(act.getActivationName(), riskObj);
        }
        data.put("activations", result.getActivationMap());
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
    private boolean checkAbstractionScript(String ruleScript, Map entity, Map<String, Object> dataCollectionMap) {
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

    private boolean checkActivationScript(String ruleScript, Map data, Map<String, Object> dataCollectionMap) {
        Object[] args = { data, dataCollectionMap };
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
