package com.pgmmers.radar.service.impl.engine.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.pgmmers.radar.enums.StrategyType;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.service.engine.strategy.ConditionEvaluator;
import com.pgmmers.radar.service.engine.strategy.StrategyExecutor;
import com.pgmmers.radar.service.engine.strategy.StrategyOutcome;
import com.pgmmers.radar.service.engine.vo.HitObject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 决策树策略执行器（v4.0）：解释执行 DEFINITION JSON——
 * 从根节点沿条件分支走到叶节点，叶节点直接产出决策（pass/review/reject + 分数 + 理由），
 * 不经过阈值推导。命中明细记录访问路径（每个经过的条件节点 + 叶节点）。
 *
 * <p>DEFINITION 结构（扁平节点表，由前端决策树编辑器产出）：</p>
 * <pre>{@code
 * {
 *   "rootId": "n1",
 *   "nodes": [
 *     {"id":"n1","type":"CONDITION","label":"白名单?","condition":{...PDCT/SMPL...},"trueChild":"n2","falseChild":"n3"},
 *     {"id":"n2","type":"LEAF","label":"白名单通过","decision":"pass","score":0,"reason":"命中白名单"},
 *     {"id":"n3","type":"LEAF","label":"非白名单","decision":"review","score":50,"reason":"需人工审核"}
 *   ]
 * }
 * }</pre>
 *
 * <p>健壮性：节点 id 重复 / 根缺失 / 分支缺失 / 叶 decision 非法 → 抛异常（由编排层按策略隔离）；
 * 执行步数上限 = 节点数 + 1，防环死循环（编辑器保存前也会做无环校验）。</p>
 */
@Component
public class DecisionTreeExecutor implements StrategyExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DecisionTreeExecutor.class);

    private static final String TYPE_CONDITION = "CONDITION";
    private static final String TYPE_LEAF = "LEAF";

    @Override
    public StrategyType type() {
        return StrategyType.DECISION_TREE;
    }

    @Override
    public StrategyOutcome execute(StrategyVO strategy, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        JsonNode definition = strategy.getDefinition();
        if (definition == null || definition.isNull() || definition.isEmpty()) {
            throw new IllegalArgumentException("decision tree definition missing, strategy=" + strategy.getStrategyName());
        }
        Map<String, JsonNode> nodeMap = indexNodes(definition);
        String rootId = text(definition, "rootId");
        if (rootId == null || !nodeMap.containsKey(rootId)) {
            throw new IllegalArgumentException("decision tree rootId missing or dangling: " + rootId);
        }

        StrategyOutcome outcome = new StrategyOutcome();
        JsonNode current = nodeMap.get(rootId);
        // 步数上限防环：正常树深度 ≤ 节点数，超过必为环
        int maxSteps = nodeMap.size() + 1;
        for (int step = 0; step < maxSteps; step++) {
            String nodeType = text(current, "type");
            String nodeId = text(current, "id");
            String label = labelOf(current);
            if (TYPE_LEAF.equals(nodeType)) {
                String decision = text(current, "decision");
                if (!"pass".equals(decision) && !"review".equals(decision) && !"reject".equals(decision)) {
                    throw new IllegalArgumentException("leaf decision invalid: " + decision + " (node " + nodeId + ")");
                }
                BigDecimal score = jsonDecimal(current.get("score"), BigDecimal.ZERO);
                outcome.setScore(score.setScale(0, 4));
                outcome.setRisk(decision);

                HitObject leafHit = new HitObject();
                leafHit.setKey(nodeId);
                String reason = text(current, "reason");
                leafHit.setDesc((reason == null || reason.isEmpty()) ? label : reason);
                leafHit.setValue(score.doubleValue());
                outcome.getHits().add(leafHit);
                outcome.getHits2().put("node_" + nodeId, leafHit);
                return outcome;
            }
            if (!TYPE_CONDITION.equals(nodeType)) {
                throw new IllegalArgumentException("node type invalid: " + nodeType + " (node " + nodeId + ")");
            }
            boolean branch = ConditionEvaluator.evaluate(current.get("condition"), data, lists);
            String childId = branch ? text(current, "trueChild") : text(current, "falseChild");
            if (childId == null || !nodeMap.containsKey(childId)) {
                throw new IllegalArgumentException("branch child missing: " + childId + " (node " + nodeId + ")");
            }

            HitObject pathHit = new HitObject();
            pathHit.setKey(nodeId);
            pathHit.setDesc(label + (branch ? "(Y)" : "(N)"));
            pathHit.setValue(0.0);
            outcome.getHits().add(pathHit);
            outcome.getHits2().put("node_" + nodeId, pathHit);

            current = nodeMap.get(childId);
        }
        throw new IllegalStateException("decision tree cycle detected, strategy=" + strategy.getStrategyName());
    }

    /** 节点索引：id → node，重复 id 抛异常（防编辑器脏数据静默吞分支） */
    private Map<String, JsonNode> indexNodes(JsonNode definition) {
        JsonNode nodes = definition.get("nodes");
        if (nodes == null || !nodes.isArray() || nodes.isEmpty()) {
            throw new IllegalArgumentException("decision tree nodes empty");
        }
        Map<String, JsonNode> nodeMap = new HashMap<>();
        for (JsonNode node : nodes) {
            String id = text(node, "id");
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("decision tree node id empty");
            }
            if (nodeMap.putIfAbsent(id, node) != null) {
                throw new IllegalArgumentException("decision tree node id duplicated: " + id);
            }
        }
        return nodeMap;
    }

    private String labelOf(JsonNode node) {
        String label = text(node, "label");
        return label == null || label.isEmpty() ? text(node, "id") : label;
    }

    private BigDecimal jsonDecimal(JsonNode node, BigDecimal defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(node.asText());
        } catch (NumberFormatException e) {
            logger.warn("invalid number in decision tree definition: {}, use default {}", node.asText(), defaultValue);
            return defaultValue;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
