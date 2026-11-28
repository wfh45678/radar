package com.pgmmers.radar.service.impl.model.transfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pgmmers.radar.enums.AggregateKind;

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
        return true;
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
        JsonNode filter = item.get("ruleDefinition");
        if (filter != null && !filter.isNull() && filter.isObject()) {
            definition.set("filter", filter);
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
}
