package com.pgmmers.radar.service.impl.model.transfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pgmmers.radar.enums.AggregateKind;

import java.io.IOException;

/**
 * v4.0（formatVersion 1.0）策略包 → v4.1（2.0）特征体系的导入转换器。
 *
 * <p>v1 包体键 preItems（ENRICH）与 abstractions（AGGREGATE）合并为 features 数组：
 * preItem 条目 → ENRICH 特征（definition: plugin/args/sourceFields，output=configJson），
 * abstraction 条目 → AGGREGATE 特征（definition: aggregateType(枚举名)/searchField/时间窗/
 * functionField/filter=原 ruleDefinition）。v2 包体直接透传。</p>
 */
public final class PackageV1Converter {

    private PackageV1Converter() {
    }

    /**
     * 就地转换：v1 信封（含 preItems/abstractions、缺 features）转换为 v2 形态。
     * v2 包体（含 features）原样返回。
     *
     * @return true=发生了 v1→v2 转换
     */
    public static boolean convertIfNeeded(ObjectNode root) {
        if (root.has("features")) {
            return false;
        }
        ArrayNode features = JsonNodeFactory.instance.arrayNode();
        if (root.has("preItems") && root.get("preItems").isArray()) {
            for (JsonNode item : root.get("preItems")) {
                features.add(convertPreItem(item));
            }
            root.remove("preItems");
        }
        if (root.has("abstractions") && root.get("abstractions").isArray()) {
            for (JsonNode item : root.get("abstractions")) {
                features.add(convertAbstraction(item));
            }
            root.remove("abstractions");
        }
        root.set("features", features);

        // activations → strategies：v1 键名改名（ModelPackage 绑定 strategies，漏改会整组丢弃，
        // 导入后规则 STRATEGY_ID 重映射为 null 触发 NOT NULL 违例）
        if (root.has("activations") && !root.has("strategies")) {
            root.set("strategies", root.get("activations"));
            root.remove("activations");
        }

        // 规则字段前缀改写：与 upgrade-4.1.0.sql 对存量行的口径一致
        // （脚本 data.preItems./data.abstractions. → data.features.；条件 JSON 与规则名同口径）
        if (root.has("rules") && root.get("rules").isArray()) {
            for (JsonNode rule : root.get("rules")) {
                rewriteRulePrefixes((ObjectNode) rule);
            }
        }

        root.put("formatVersion", "2.0");
        return true;
    }

    /** 规则内 v1 前缀改写（scripts / ruleDefinition / abstractionName）。 */
    private static void rewriteRulePrefixes(ObjectNode rule) {
        JsonNode scripts = rule.get("scripts");
        if (scripts != null && scripts.isTextual()) {
            rule.put("scripts", rewriteText(scripts.asText()));
        }
        JsonNode rd = rule.get("ruleDefinition");
        if (rd != null && rd.isTextual()) {
            rule.put("ruleDefinition", rewriteJsonPrefixes(rd.asText()));
        } else if (rd != null && rd.isObject()) {
            rule.put("ruleDefinition", rewriteJsonPrefixes(rd.toString()));
        }
        JsonNode name = rule.get("abstractionName");
        if (name != null && name.isTextual()) {
            rule.put("abstractionName", name.asText().replace("abstractions.", "features."));
        }
    }

    private static String rewriteText(String script) {
        return script.replace("data.preItems.", "data.features.")
                .replace("data.abstractions.", "data.features.");
    }

    private static String rewriteJsonPrefixes(String json) {
        return json.replace("\"preItems.", "\"features.")
                .replace("\"abstractions.", "\"features.");
    }

    /** preItem 条目 → ENRICH 特征 */
    private static ObjectNode convertPreItem(JsonNode item) {
        ObjectNode feature = JsonNodeFactory.instance.objectNode();
        feature.put("featureName", text(item, "destField"));
        feature.put("type", "ENRICH");
        feature.put("label", text(item, "label"));
        feature.put("status", intOr(item, "status", 1));

        ObjectNode definition = JsonNodeFactory.instance.objectNode();
        definition.put("plugin", text(item, "plugin"));
        definition.put("reqType", text(item, "reqType"));
        // args 原样透传为字符串：JSON 对象串或存量自由文本，执行器按 JSON_VALID 语义解析
        JsonNode args = item.get("args");
        definition.put("args", args == null || args.isNull() ? null : args.asText());
        // sourceField 逗号分隔 → 数组
        ArrayNode sourceFields = definition.putArray("sourceFields");
        String sourceField = text(item, "sourceField");
        if (sourceField != null && !sourceField.isEmpty()) {
            for (String s : sourceField.split(",")) {
                sourceFields.add(s.trim());
            }
        }
        // DEFINITION 与 OUTPUT 同约定：包内为 JSON 字符串形态（绑定 FeaturePO.String 字段）
        feature.put("definition", definition.toString());

        JsonNode configJson = item.get("configJson");
        if (configJson != null && !configJson.isNull() && configJson.isObject()) {
            // OUTPUT 与 DEFINITION 同约定：包内为 JSON 字符串形态
            feature.put("output", configJson.toString());
        }
        return feature;
    }

    /** abstraction 条目 → AGGREGATE 特征 */
    private static ObjectNode convertAbstraction(JsonNode item) {
        ObjectNode feature = JsonNodeFactory.instance.objectNode();
        feature.put("featureName", text(item, "name"));
        feature.put("type", "AGGREGATE");
        feature.put("label", text(item, "label"));
        feature.put("status", intOr(item, "status", 1));
        feature.put("comment", text(item, "comment"));

        ObjectNode definition = JsonNodeFactory.instance.objectNode();
        // aggregateType：旧 int 常量 → 枚举名
        AggregateKind kind = AggregateKind.ofCode(item.path("aggregateType").asInt(-1));
        if (kind != null) {
            definition.put("aggregateType", kind.name());
        }
        definition.put("searchField", text(item, "searchField"));
        definition.put("searchIntervalType", intOr(item, "searchIntervalType", 5));
        definition.put("searchIntervalValue", intOr(item, "searchIntervalValue", 1));
        definition.put("functionField", text(item, "functionField"));
        // v1 包内 ruleDefinition 为 JSON 字符串形态（RulePO 同构），解析回对象；列前缀同步改写
        JsonNode filter = item.get("ruleDefinition");
        if (filter != null && !filter.isNull()) {
            if (filter.isObject()) {
                definition.set("filter", rewriteJsonNodePrefixes((ObjectNode) filter));
            } else if (filter.isTextual() && !filter.asText().isEmpty()) {
                try {
                    JsonNode parsed = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readTree(filter.asText());
                    if (parsed != null && parsed.isObject()) {
                        definition.set("filter", rewriteJsonNodePrefixes((ObjectNode) parsed));
                    }
                } catch (IOException ignore) {
                    // 非法条件 JSON：不设置 filter（无过滤语义）
                }
            }
        }
        feature.put("definition", definition.toString());
        return feature;
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private static int intOr(JsonNode node, String field, int defaultValue) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? defaultValue : v.asInt(defaultValue);
    }

    /** 条件树列前缀改写：v1 编辑态（preItems./abstractions.）→ v4.1 features.。 */
    private static ObjectNode rewriteJsonNodePrefixes(ObjectNode node) {
        String json = node.toString().replace("\"preItems.", "\"features.")
                .replace("\"abstractions.", "\"features.");
        try {
            return (ObjectNode) new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
        } catch (IOException e) {
            return node;
        }
    }
}
