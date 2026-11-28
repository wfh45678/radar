package com.pgmmers.radar.service.impl.engine.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.pgmmers.radar.enums.StrategyType;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.service.engine.strategy.StrategyExecutor;
import com.pgmmers.radar.service.engine.strategy.StrategyOutcome;
import com.pgmmers.radar.service.engine.vo.HitObject;
import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 名单匹配策略执行器（v5.1）：事件字段值与预加载数据名单（黑白名单）比对——
 * **命中 → 直出配置的命中决策（reject/review/pass + 得分 + 理由）**；
 * 未命中 → 走缺省放行（pass/0，理由"未命中名单"），名单不存在同样按未命中降级（告警日志）。
 *
 * <p>DEFINITION 结构（由前端名单匹配编辑器产出）：</p>
 * <pre>{@code
 * {
 *   "dataList":    "手机号黑名单",        // 数据名单名称（黑白名单管理页同源；lists 缓存键）
 *   "matchField":  "fields.mobile",       // 事件取值字段（fields./features. 前缀，执行期剥离）
 *   "hitDecision": "reject",
 *   "hitScore":    100,
 *   "hitReason":   "命中黑名单"
 * }
 * }</pre>
 *
 * <p>名单比对复用规则条件 InList 的同一缓存语义（{@code lists}：名单名 → {记录值 → ""}，
 * containsKey 严格相等）；命中明细：一条 HitObject（key=list_名单名，desc=理由 + 命中值）。</p>
 */
@Component
public class ListMatchExecutor implements StrategyExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ListMatchExecutor.class);

    @Override
    public StrategyType type() {
        return StrategyType.LIST_MATCH;
    }

    @Override
    public StrategyOutcome execute(StrategyVO strategy, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        JsonNode definition = strategy.getDefinition();
        if (definition == null || definition.isNull() || definition.isEmpty()) {
            throw new IllegalArgumentException("list match definition missing, strategy=" + strategy.getStrategyName());
        }
        StrategyOutcome outcome = new StrategyOutcome();

        String listName = text(definition, "dataList");
        String matchField = stripScope(text(definition, "matchField"));
        String hitReason = text(definition, "hitReason");
        String desc = (hitReason == null || hitReason.isEmpty())
                ? "名单匹配：" + (listName == null ? "未指定名单" : listName) + " → 未命中"
                : hitReason + " → 未命中";

        Object value = resolveValue(data, matchField);
        boolean hit = false;
        if (value == null) {
            logger.debug("list match field value is null, strategy={} (matchField={})",
                    strategy.getStrategyName(), matchField);
        } else if (listName == null || lists.get(listName) == null) {
            logger.warn("data list not found in cache, strategy={} (dataList={})",
                    strategy.getStrategyName(), listName);
        } else if (lists.get(listName) instanceof Map<?, ?> recordMap) {
            hit = value != null && recordMap.containsKey(String.valueOf(value));
        }

        if (hit) {
            BigDecimal score = jsonDecimal(definition.get("hitScore"), BigDecimal.ZERO);
            String decision = text(definition, "hitDecision");
            HitObject hitObject = new HitObject();
            hitObject.setKey("list_" + listName);
            hitObject.setDesc((hitReason == null || hitReason.isEmpty()) ? "命中名单" : hitReason
                    + "（值：" + value + "）");
            hitObject.setValue(score.doubleValue());
            outcome.getHits().add(hitObject);
            outcome.getHits2().put("list_" + listName, hitObject);
            outcome.setRisk(decision == null || decision.isEmpty() ? "reject" : decision);
            outcome.setScore(score);
            return outcome;
        }

        // 未命中（含名单缺失/取值缺失降级）：缺省放行
        HitObject miss = new HitObject();
        miss.setKey("list_" + (listName == null ? "none" : listName));
        miss.setDesc(desc);
        miss.setValue(0.0);
        outcome.getHits().add(miss);
        outcome.getHits2().put("list_" + (listName == null ? "none" : listName), miss);
        JsonNode missDecision = definition.get("missDecision");
        outcome.setRisk(missDecision == null || missDecision.isNull() ? "pass" : missDecision.asText("pass"));
        return outcome;
    }

    /** 取值：fields → features → 嵌套点路径（编辑态 scope 前缀剥离，同 AggregateExecutor 口径）。 */
    private Object resolveValue(Map<String, Map<String, ?>> data, String column) {
        if (column == null || column.isEmpty()) {
            return null;
        }
        String bare = stripScope(column);
        String[] parts = bare.split("\\.");
        if (parts.length == 1) {
            for (String group : new String[] {"fields", "features"}) {
                Map<String, ?> groupMap = data.get(group);
                if (groupMap != null && groupMap.containsKey(parts[0])) {
                    return groupMap.get(parts[0]);
                }
            }
            return null;
        }
        Object current = data.get(parts[0]);
        for (int i = 1; i < parts.length && current != null; i++) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(parts[i]);
        }
        return current;
    }

    private String stripScope(String column) {
        if (column == null) {
            return "";
        }
        return column.replace("fields.", "").replace("features.", "").replace("preItems.", "");
    }

    private BigDecimal jsonDecimal(JsonNode node, BigDecimal defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(node.asText());
        } catch (NumberFormatException e) {
            logger.warn("invalid number in list match definition: {}, use default {}", node.asText(), defaultValue);
            return defaultValue;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
