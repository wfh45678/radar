package com.pgmmers.radar.service.impl.engine.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.pgmmers.radar.enums.StrategyType;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.service.engine.strategy.ConditionEvaluator;
import com.pgmmers.radar.service.engine.strategy.StrategyExecutor;
import com.pgmmers.radar.service.engine.strategy.StrategyOutcome;
import com.pgmmers.radar.service.engine.vo.HitObject;
import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 评分卡策略执行器（v4.0）：解释执行 DEFINITION JSON——
 * 总分 = initialScore + Σ(特征首个命中分箱得分 × 权重，无命中取 defaultScore)，
 * 决策沿用策略阈值（sum ≥ high → reject，≥ median → review，否则 pass）。
 *
 * <p>DEFINITION 结构（features/bins 由前端评分卡编辑器产出）：</p>
 * <pre>{@code
 * {
 *   "initialScore": 0,
 *   "features": [
 *     {
 *       "name": "f_ip_qty", "label": "IP交易频次",
 *       "column": "features.tran_ip_1_day_qty",       // 特征变量（fields/features 路径，v4.0 包兼容旧前缀）
 *       "weight": 1.0, "defaultScore": 0,
 *       "bins": [
 *         {"label": "<10",  "operator": "Less",         "value": "10", "score": -10},
 *         {"label": ">=30", "operator": "Greater_Equal","value": "30", "score": 30}
 *       ]
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <p>分箱按数组顺序求值、首个命中生效（区间建议互斥且覆盖，编辑器负责校验提示）；
 * 操作符复用 {@code ConditionOperator}（支持 InList 名单分箱）；条件求值统一走
 * {@link ConditionEvaluator}。命中明细：每特征一条 HitObject（key=特征 name，desc=标签+命中分箱）。</p>
 */
@Component
public class ScoreCardExecutor implements StrategyExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ScoreCardExecutor.class);

    @Override
    public StrategyType type() {
        return StrategyType.SCORECARD;
    }

    @Override
    public StrategyOutcome execute(StrategyVO strategy, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        JsonNode definition = strategy.getDefinition();
        if (definition == null || definition.isNull() || definition.isEmpty()) {
            throw new IllegalArgumentException("scorecard definition missing, strategy=" + strategy.getStrategyName());
        }
        StrategyOutcome outcome = new StrategyOutcome();

        BigDecimal total = jsonDecimal(definition.get("initialScore"), BigDecimal.ZERO);
        JsonNode features = definition.get("features");
        if (features != null && features.isArray()) {
            for (JsonNode feature : features) {
                total = total.add(scoreFeature(feature, data, lists, outcome));
            }
        }

        total = total.setScale(0, 4);
        outcome.setScore(total);
        outcome.setRisk(RuleSetExecutor.riskLevel(total, strategy.getHigh(), strategy.getMedian()));
        return outcome;
    }

    /** 单特征计分：首个命中分箱得分 × 权重（无命中取 defaultScore），并写命中明细 */
    private BigDecimal scoreFeature(JsonNode feature, Map<String, Map<String, ?>> data, Map<String, ?> lists,
                                    StrategyOutcome outcome) {
        String name = text(feature, "name");
        String label = text(feature, "label");
        if (label == null || label.isEmpty()) {
            label = name;
        }
        String column = text(feature, "column");

        BigDecimal score = jsonDecimal(feature.get("defaultScore"), BigDecimal.ZERO);
        String matchedBin = "default";
        JsonNode bins = feature.get("bins");
        if (bins != null && bins.isArray()) {
            for (JsonNode bin : bins) {
                if (ConditionEvaluator.evaluate(binCondition(bin, column), data, lists)) {
                    score = jsonDecimal(bin.get("score"), BigDecimal.ZERO);
                    matchedBin = text(bin, "label");
                    break;
                }
            }
        }

        BigDecimal weight = jsonDecimal(feature.get("weight"), BigDecimal.ONE);
        BigDecimal contribution = score.multiply(weight);

        HitObject hit = new HitObject();
        hit.setKey(name);
        hit.setDesc(label + ":" + (matchedBin == null ? "default" : matchedBin));
        hit.setValue(contribution.setScale(2, 4).doubleValue());
        outcome.getHits().add(hit);
        outcome.getHits2().put("feature_" + name, hit);
        return contribution;
    }

    /** 分箱条件包装为 SMPL 单条件：expressions[0]=特征变量（ENTATTR），expressions[1]=分箱阈值（CONST） */
    private JsonNode binCondition(JsonNode bin, String column) {
        com.fasterxml.jackson.databind.node.ObjectNode condition =
                com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        condition.put("class", "SMPL");
        condition.put("enabled", true);
        condition.put("operator", text(bin, "operator"));
        com.fasterxml.jackson.databind.node.ArrayNode expressions = condition.putArray("expressions");
        com.fasterxml.jackson.databind.node.ObjectNode left = expressions.addObject();
        left.put("class", "ENTATTR");
        left.put("column", column);
        com.fasterxml.jackson.databind.node.ObjectNode right = expressions.addObject();
        right.put("class", "CONST");
        right.put("value", text(bin, "value"));
        return condition;
    }

    private BigDecimal jsonDecimal(JsonNode node, BigDecimal defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(node.asText());
        } catch (NumberFormatException e) {
            logger.warn("invalid number in scorecard definition: {}, use default {}", node.asText(), defaultValue);
            return defaultValue;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
