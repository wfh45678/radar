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
 * 决策表策略执行器（v5.1）：解释执行 DEFINITION JSON——
 * 决策行按数组顺序求值，**首个全条件命中行**直出决策（pass/review/reject + 得分 + 理由）；
 * 无行命中走缺省决策（defaultDecision/defaultScore，缺省 pass/0）。
 *
 * <p>DEFINITION 结构（rows 由前端决策表编辑器产出）：</p>
 * <pre>{@code
 * {
 *   "defaultDecision": "pass",
 *   "defaultScore": 0,
 *   "rows": [
 *     {
 *       "label": "大额夜间交易",
 *       "conditions": [
 *         {"column": "features.tran_ip_1_day_qty", "operator": "Greater",        "value": "30"},
 *         {"column": "features.tran_ip_1_day_amt", "operator": "Greater_Equal",  "value": "1000"}
 *       ],                                    // 行内条件 AND 语义
 *       "decision": "reject", "score": 100, "reason": "高频大额"
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <p>行条件复用 {@link ConditionEvaluator}（SMPL 单条件：ENTATTR 特征变量 + CONST 阈值，
 * 操作符同 {@code ConditionOperator}）；命中明细：命中行一条 HitObject（key=row_行号，
 * desc=行标签 + 命中条件数），无命中时缺省决策也产出一条明细（key=default）。</p>
 */
@Component
public class DecisionTableExecutor implements StrategyExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DecisionTableExecutor.class);

    @Override
    public StrategyType type() {
        return StrategyType.DECISION_TABLE;
    }

    @Override
    public StrategyOutcome execute(StrategyVO strategy, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        JsonNode definition = strategy.getDefinition();
        if (definition == null || definition.isNull() || definition.isEmpty()) {
            throw new IllegalArgumentException("decision table definition missing, strategy=" + strategy.getStrategyName());
        }
        StrategyOutcome outcome = new StrategyOutcome();

        JsonNode rows = definition.get("rows");
        if (rows != null && rows.isArray()) {
            int index = 0;
            for (JsonNode row : rows) {
                if (rowMatches(row, data, lists)) {
                    applyOutcome(outcome, row, "row_" + index);
                    return outcome;
                }
                index++;
            }
        }

        // 无行命中：缺省决策
        JsonNode defaultNode = definition.get("defaultDecision");
        String defaultDecision = defaultNode == null || defaultNode.isNull() ? "pass" : defaultNode.asText("pass");
        BigDecimal defaultScore = jsonDecimal(definition.get("defaultScore"), BigDecimal.ZERO);
        HitObject hit = new HitObject();
        hit.setKey("default");
        hit.setDesc("未命中任何决策行");
        hit.setValue(defaultScore.doubleValue());
        outcome.getHits().add(hit);
        outcome.getHits2().put("default", hit);
        outcome.setRisk(defaultDecision);
        outcome.setScore(defaultScore);
        return outcome;
    }

    /** 行命中判定：行内全部条件 AND 成立 */
    private boolean rowMatches(JsonNode row, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        JsonNode conditions = row.get("conditions");
        if (conditions == null || !conditions.isArray() || conditions.isEmpty()) {
            return false;
        }
        for (JsonNode condition : conditions) {
            if (!ConditionEvaluator.evaluate(binCondition(condition), data, lists)) {
                return false;
            }
        }
        return true;
    }

    /** 行命中时直出决策/得分/理由，并写命中明细 */
    private void applyOutcome(StrategyOutcome outcome, JsonNode row, String hitKey) {
        String label = text(row, "label");
        String reason = text(row, "reason");
        BigDecimal score = jsonDecimal(row.get("score"), BigDecimal.ZERO);

        HitObject hit = new HitObject();
        hit.setKey(hitKey);
        hit.setDesc((reason == null || reason.isEmpty()) ? (label == null ? hitKey : label) : reason);
        hit.setValue(score.doubleValue());
        outcome.getHits().add(hit);
        outcome.getHits2().put(hitKey, hit);

        JsonNode decision = row.get("decision");
        outcome.setRisk(decision == null || decision.isNull() ? "pass" : decision.asText("pass"));
        outcome.setScore(score);
    }

    /** 行条件包装为 SMPL 单条件：expressions[0]=特征变量（ENTATTR），expressions[1]=阈值（CONST） */
    private JsonNode binCondition(JsonNode condition) {
        com.fasterxml.jackson.databind.node.ObjectNode smpl =
                com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        smpl.put("class", "SMPL");
        smpl.put("enabled", true);
        smpl.put("operator", text(condition, "operator"));
        com.fasterxml.jackson.databind.node.ArrayNode expressions = smpl.putArray("expressions");
        com.fasterxml.jackson.databind.node.ObjectNode left = expressions.addObject();
        left.put("class", "ENTATTR");
        left.put("column", text(condition, "column"));
        com.fasterxml.jackson.databind.node.ObjectNode right = expressions.addObject();
        right.put("class", "CONST");
        right.put("value", text(condition, "value"));
        return smpl;
    }

    private BigDecimal jsonDecimal(JsonNode node, BigDecimal defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(node.asText());
        } catch (NumberFormatException e) {
            logger.warn("invalid number in decision table definition: {}, use default {}", node.asText(), defaultValue);
            return defaultValue;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
