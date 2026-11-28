package com.pgmmers.radar.service.engine.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.pgmmers.radar.service.engine.strategy.ConditionEvaluator;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 衍生表达式求值器（v4.1 特征工程）：解释 DEFINITION.expression 的 AST JSON——
 * 节点 = {fn:"函数名", args:[...]} / {column:"features.xxx"} / {const:"字面量", type:"STRING|DOUBLE"}。
 * 列取值经 {@link ConditionEvaluator#resolveColumn}（支持点路径与旧前缀 alias）。
 *
 * <p>函数注册表：内置函数（{@code StandardDerivedFunctions}）+ 容器内自定义
 * {@link DerivedFunction} bean 自动收集，同名单函数以首个内置为准并告警。
 * 求值失败（未知函数/参数不合法/运算除零）返回 null——特征无产出，不抛异常。</p>
 */
@Component
public class ExpressionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionEvaluator.class);

    private final Map<String, DerivedFunction> functions = new HashMap<>();

    @Autowired
    public ExpressionEvaluator(List<DerivedFunction> customFunctions) {
        // 内置函数
        for (DerivedFunction builtin : new DerivedFunction[]{
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Add(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Sub(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Mul(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Div(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Abs(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Round(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Concat(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Substring(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Upper(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Lower(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.RegexExtract(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Mapping(),
                new com.pgmmers.radar.service.impl.engine.feature.StandardDerivedFunctions.Coalesce()}) {
            functions.put(builtin.name().toUpperCase(), builtin);
        }
        // 自定义函数（同名以内置优先）
        for (DerivedFunction custom : customFunctions) {
            DerivedFunction prev = functions.putIfAbsent(custom.name().toUpperCase(), custom);
            if (prev != null) {
                logger.warn("duplicate DerivedFunction name {}: keep builtin, ignore {}",
                        custom.name(), custom.getClass().getSimpleName());
            }
        }
        logger.info("expression evaluator initialized with {} functions", functions.size());
    }

    /**
     * 求值表达式 AST。
     *
     * @param expression 表达式节点（{fn, args}）
     * @param data       求值上下文
     * @return 结果（BigDecimal/String）或 null（无产出/求值失败）
     */
    public Object evaluate(JsonNode expression, Map<String, Map<String, ?>> data) {
        return evalNode(expression, data);
    }

    private Object evalNode(JsonNode node, Map<String, Map<String, ?>> data) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.has("fn")) {
            return evalFunction(node, data);
        }
        if (node.has("column")) {
            return ConditionEvaluator.resolveColumn(node.path("column").asText(""), data);
        }
        if (node.has("const")) {
            JsonNode value = node.get("const");
            String type = node.path("type").asText("STRING");
            if ("DOUBLE".equalsIgnoreCase(type) || "INTEGER".equalsIgnoreCase(type) || "LONG".equalsIgnoreCase(type)) {
                try {
                    return new BigDecimal(value.asText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return value.asText();
        }
        logger.warn("unknown expression node, treat as null");
        return null;
    }

    private Object evalFunction(JsonNode node, Map<String, Map<String, ?>> data) {
        String fn = node.path("fn").asText("");
        DerivedFunction function = functions.get(fn.toUpperCase());
        if (function == null) {
            logger.warn("unknown derived function: {}, treat as null", fn);
            return null;
        }
        JsonNode argsNode = node.get("args");
        int argCount = argsNode == null || !argsNode.isArray() ? 0 : argsNode.size();
        if (argCount < function.minArgs()
                || (function.maxArgs() != -1 && argCount > function.maxArgs())) {
            logger.warn("derived function {} arity invalid (got {}), treat as null", fn, argCount);
            return null;
        }
        java.util.List<Object> args = new java.util.ArrayList<>(argCount);
        for (JsonNode arg : argsNode) {
            args.add(evalNode(arg, data));
        }
        try {
            return function.apply(args);
        } catch (Exception e) {
            logger.warn("derived function {} threw, treat as null", fn, e);
            return null;
        }
    }
}
