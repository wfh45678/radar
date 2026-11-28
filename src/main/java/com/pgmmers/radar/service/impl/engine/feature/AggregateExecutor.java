package com.pgmmers.radar.service.impl.engine.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.pgmmers.radar.config.EngineMetrics;
import com.pgmmers.radar.common.util.DateUtils;
import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.model.vo.model.FieldVO;
import com.pgmmers.radar.model.vo.model.ModelVO;
import com.pgmmers.radar.service.engine.feature.AggregateFunction;
import com.pgmmers.radar.service.engine.feature.AggregateQuery;
import com.pgmmers.radar.service.engine.feature.FeatureExecutor;
import com.pgmmers.radar.service.engine.feature.FeatureOutcome;
import com.pgmmers.radar.service.engine.strategy.ConditionEvaluator;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.service.model.ModelService;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 统计聚合特征执行器（v4.1，原 v3.x Abstraction executeAbstraction 逻辑）：
 * filter 条件树经 {@link ConditionEvaluator} 后端解释（Groovy 脚本已退役），
 * 时间窗聚合经 AggregateFunction 注册表（F3 注册表化），产出 features.&lt;featureName&gt;。
 * 条件不命中以 -1 哨兵占位（沿袭 v3.x 语义）；无数据/配置异常时返回 empty 由编排层跳过。
 */
@Component
public class AggregateExecutor implements FeatureExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AggregateExecutor.class);

    @Autowired
    private AggregateFunctionRegistry functionRegistry;

    @Autowired
    private ModelService modelService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private EngineMetrics engineMetrics;

    @Override
    public FeatureType type() {
        return FeatureType.AGGREGATE;
    }

    @Override
    public FeatureOutcome execute(FeatureVO feature, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        JsonNode definition = feature.getDefinition();
        if (definition == null || definition.isNull() || definition.isEmpty()) {
            logger.warn("aggregate definition missing, skip feature={}", feature.getFeatureName());
            engineMetrics.recordFeatureExecution(FeatureType.AGGREGATE.name(), "error");
            return FeatureOutcome.empty();
        }
        AggregateKind kind = AggregateKind.of(definition.path("aggregateType").asText(null));
        if (kind == null) {
            logger.warn("unknown aggregateType: {}, skip feature={}",
                    definition.path("aggregateType").asText(""), feature.getFeatureName());
            engineMetrics.recordFeatureExecution(FeatureType.AGGREGATE.name(), "error");
            return FeatureOutcome.empty();
        }

        // 过滤条件：条件树后端解释
        boolean matched = ConditionEvaluator.evaluate(definition.get("filter"), data, lists);
        if (!matched) {
            // 条件不命中的特征以 -1 哨兵占位（沿袭 v3.x 语义，报表/历史图可区分）
            return FeatureOutcome.of(-1);
        }

        Long modelId = feature.getModelId();
        ModelVO model = modelService.getModelById(modelId);
        String refDateFieldName = model.getReferenceDate();
        Date refDate;
        try {
            Long refDatetimeMills = (Long) data.get("fields").get(refDateFieldName);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(refDatetimeMills);
            refDate = c.getTime();
        } catch (Exception e) {
            logger.warn("reference date invalid, skip feature={}", feature.getFeatureName());
            return FeatureOutcome.empty();
        }
        int dateType = definition.path("searchIntervalType").asInt(5);
        int interval = definition.path("searchIntervalValue").asInt(1);
        Date beginDate = DateUtils.addDate(refDate, dateType, interval * -1).getTime();

        // 搜索键/统计键（编辑态带前缀，执行期剥离；取值 fields → features → 嵌套路径）
        String searchField = stripScopePrefix(definition.path("searchField").asText(""));
        String functionField = stripScopePrefix(definition.path("functionField").asText(""));
        Object searchFieldVal = resolveValue(data, searchField);
        if (searchFieldVal == null) {
            logger.warn("search field value is null, skip feature={} (searchField={})",
                    feature.getFeatureName(), searchField);
            return FeatureOutcome.empty();
        }

        FieldType functionFieldType = null;
        if (!StringUtils.isEmpty(functionField)) {
            for (FieldVO field : fieldService.listField(modelId)) {
                if (field.getFieldName().equals(functionField)) {
                    functionFieldType = FieldType.valueOf(field.getFieldType());
                    break;
                }
            }
            if (functionFieldType == null) {
                // 特征来源字段无字段类型，降级 STRING（历史行为）
                functionFieldType = FieldType.valueOf("STRING");
            }
        }

        engineMetrics.recordFeatureExecution(FeatureType.AGGREGATE.name(), "success");
        AggregateQuery query = AggregateQuery.builder()
                .modelId(modelId.toString())
                .searchField(searchField)
                .searchFieldValue(searchFieldVal)
                .refDateFieldName(refDateFieldName)
                .begin(beginDate)
                .end(refDate)
                .functionField(functionField)
                .functionFieldType(functionFieldType)
                .build();
        Object calResult = switchAggregate(kind, feature, query);
        if (calResult instanceof BigDecimal) {
            return FeatureOutcome.of(((BigDecimal) calResult).doubleValue());
        }
        return FeatureOutcome.of(calResult);
    }

    /** 聚合分发（v4.1：AggregateFunction 注册表，替换 v3.x switch-case）。 */
    private Object switchAggregate(AggregateKind kind, FeatureVO feature, AggregateQuery query) {
        AggregateFunction function = functionRegistry.get(kind);
        if (function == null) {
            logger.warn("no AggregateFunction registered for kind {}, skip feature={}",
                    kind, feature.getFeatureName());
            return null;
        }
        return function.aggregate(query);
    }

    /** 剥离编辑态 scope 前缀（fields./features.，兼容历史 preItems.）。 */
    private String stripScopePrefix(String column) {
        if (column == null) {
            return "";
        }
        return column.replace("fields.", "").replace("features.", "").replace("preItems.", "");
    }

    /** 特征取值：fields → features → features 内嵌套点路径。 */
    private Object resolveValue(Map<String, Map<String, ?>> data, String name) {
        Object val = data.get("fields").get(name);
        if (val == null && data.get("features") != null) {
            val = data.get("features").get(name);
            if (val == null) {
                val = com.pgmmers.radar.service.impl.util.JsonParserUtil.value(data.get("features"), name, null);
            }
        }
        return val;
    }
}
