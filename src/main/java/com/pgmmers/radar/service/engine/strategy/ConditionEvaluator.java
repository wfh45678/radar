package com.pgmmers.radar.service.engine.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.pgmmers.radar.enums.ConditionOperator;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 后端条件求值器（v4.0）：直接解释 ruleDefinition 同构 JSON（PDCT/SMPL + ENTATTR/CONST），
 * 不再依赖前端生成的 Groovy。服务评分卡/决策树等后端解释型策略；
 * 规则集仍走 Groovy 通路（行为不变），后续版本可迁移到本求值器。
 *
 * <p>求值语义与前端编译器（frontend/src/component/utils/groovyUtil.jsx）逐条对齐：</p>
 * <ul>
 *   <li>PDCT.linking：All/NotAll → &amp;&amp;，Any/None → ||，NotAll/None 整体取反（与前端生成一致）；</li>
 *   <li>操作符：见 {@link ConditionOperator}（20 个，含 Field_ 前缀字段对字段比较）；</li>
 *   <li>数值比较用 BigDecimal（等值/排序），任一侧非数值回退字符串比较；</li>
 *   <li>InList/NotInList：lists 为预加载名单（name → Map 形态，containsKey 语义）；名单缺失时
 *       InList 为 false、NotInList 为 true；</li>
 *   <li>IsNull/IsNotNull：Groovy truthiness（null/空串/0/空集合 为"空"）；</li>
 *   <li>取值缺失（字段不存在/null）：排序与包含类操作符一律 false，Equal 双 null 为 true。</li>
 * </ul>
 *
 * <p>本类为无状态静态工具，便于单测与策略执行器直接调用。</p>
 */
public final class ConditionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ConditionEvaluator.class);

    private ConditionEvaluator() {
    }

    /**
     * 求值一棵条件树。
     *
     * @param definition 条件 JSON（class=PDCT 复合条件 或 SMPL 单条件）
     * @param data       求值上下文 {fields, preItems, abstractions}
     * @param lists      预加载名单（name → Map）
     * @return 条件是否成立；definition 为空按 false 处理
     */
    public static boolean evaluate(JsonNode definition, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        if (definition == null || definition.isNull() || definition.isEmpty()) {
            return false;
        }
        return evalNode(definition, data, lists);
    }

    private static boolean evalNode(JsonNode node, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        String clazz = text(node, "class");
        if ("PDCT".equals(clazz)) {
            return evalComposite(node, data, lists);
        }
        if ("SMPL".equals(clazz)) {
            return evalSimple(node, data, lists);
        }
        logger.warn("unknown condition class: {}, treat as false", clazz);
        return false;
    }

    /** PDCT 复合条件：linking 组合子条件，NotAll/None 整体取反（对齐前端 processRules） */
    private static boolean evalComposite(JsonNode node, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        String linking = text(node, "linking");
        JsonNode conditions = node.get("conditions");
        if (conditions == null || !conditions.isArray() || conditions.isEmpty()) {
            return false;
        }
        boolean and = "All".equals(linking) || "NotAll".equals(linking);
        boolean negate = "NotAll".equals(linking) || "None".equals(linking);
        boolean result = and;
        for (JsonNode child : conditions) {
            boolean branchValue = evalNode(child, data, lists);
            if (and) {
                result &= branchValue;
                if (!result) {
                    break;
                }
            } else {
                result |= branchValue;
                if (result) {
                    break;
                }
            }
        }
        return negate != result;
    }

    /** SMPL 单条件：expressions[0] op expressions[1]（IsNull/IsNotNull 仅需左操作数） */
    private static boolean evalSimple(JsonNode node, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        ConditionOperator operator = ConditionOperator.of(text(node, "operator"));
        if (operator == null) {
            logger.warn("unknown condition operator: {}, treat as false", text(node, "operator"));
            return false;
        }
        JsonNode expressions = node.get("expressions");
        if (expressions == null || !expressions.isArray() || expressions.isEmpty()) {
            return false;
        }
        Object left = resolveExpression(expressions.get(0), data, lists);
        if (operator == ConditionOperator.IsNull) {
            return !truthy(left);
        }
        if (operator == ConditionOperator.IsNotNull) {
            return truthy(left);
        }
        if (expressions.size() < 2) {
            return false;
        }
        Object right = resolveExpression(expressions.get(1), data, lists);
        return apply(operator, left, right, lists);
    }

    /** 表达式取值：ENTATTR=上下文取值（column 形如 fields.xxx），CONST=字面量（LIST 类型值为名单名） */
    public static Object resolveExpression(JsonNode expr, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        if (expr == null || expr.isNull()) {
            return null;
        }
        String clazz = text(expr, "class");
        if ("ENTATTR".equals(clazz)) {
            return resolveColumn(text(expr, "column"), data);
        }
        if ("CONST".equals(clazz)) {
            JsonNode value = expr.get("value");
            return value == null || value.isNull() ? null : value.asText();
        }
        logger.warn("unknown expression class: {}", clazz);
        return null;
    }

    /**
     * 按列路径取上下文值：column 形如 {@code fields.xxx} / {@code preItems.xxx} / {@code abstractions.xxx}；
     * 无点号时回退 abstractions 再 fields（兼容旧抽象引用写法）。
     */
    public static Object resolveColumn(String column, Map<String, Map<String, ?>> data) {
        if (column == null || column.isEmpty() || data == null) {
            return null;
        }
        int dot = column.indexOf('.');
        if (dot == -1) {
            Object val = lookup(data, "abstractions", column);
            return val != null ? val : lookup(data, "fields", column);
        }
        return lookup(data, column.substring(0, dot), column.substring(dot + 1));
    }

    private static Object lookup(Map<String, Map<String, ?>> data, String scope, String key) {
        Map<String, ?> scopeMap = data.get(scope);
        return scopeMap == null ? null : scopeMap.get(key);
    }

    /** 单操作数运算（InList 类需要 lists） */
    private static boolean apply(ConditionOperator operator, Object left, Object right, Map<String, ?> lists) {
        switch (operator) {
        case StartsWith:
            return str(left) != null && str(right) != null && str(left).startsWith(str(right));
        case NotStartsWith:
            return !(str(left) != null && str(right) != null && str(left).startsWith(str(right)));
        case Contains:
            return str(left) != null && str(right) != null && str(left).contains(str(right));
        case NotContains:
            return !(str(left) != null && str(right) != null && str(left).contains(str(right)));
        case Equal:
        case Field_Equal:
            return compareEqual(left, right);
        case NotEqual:
        case Field_Not_Equal:
            return !compareEqual(left, right);
        case Less:
        case Field_Less: {
            Integer cmp = order(left, right);
            return cmp != null && cmp < 0;
        }
        case Less_Equal:
        case Field_Less_Equal: {
            Integer cmp = order(left, right);
            return cmp != null && cmp <= 0;
        }
        case Greater:
        case Field_Greater: {
            Integer cmp = order(left, right);
            return cmp != null && cmp > 0;
        }
        case Greater_Equal:
        case Field_Greater_Equal: {
            Integer cmp = order(left, right);
            return cmp != null && cmp >= 0;
        }
        case InList:
            return inList(left, right, lists);
        case NotInList:
            return !inList(left, right, lists);
        default:
            logger.warn("unhandled operator: {}", operator);
            return false;
        }
    }

    /** 名单包含判断：lists[name] 为 Map（containsKey 语义）；名单缺失时视为不包含 */
    @SuppressWarnings("unchecked")
    private static boolean inList(Object left, Object listName, Map<String, ?> lists) {
        if (str(listName) == null || lists == null) {
            return false;
        }
        Object list = lists.get(str(listName));
        if (list instanceof Map) {
            return str(left) != null && ((Map<Object, Object>) list).containsKey(str(left));
        }
        if (list instanceof Collection) {
            return str(left) != null && ((Collection<Object>) list).contains(str(left));
        }
        return false;
    }

    /** 等值比较：双方可解析数值按 BigDecimal 比较，否则按字符串；双 null 相等，单 null 不等 */
    private static boolean compareEqual(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        BigDecimal ln = toNumber(left);
        BigDecimal rn = toNumber(right);
        if (ln != null && rn != null) {
            return ln.compareTo(rn) == 0;
        }
        return str(left).equals(str(right));
    }

    /** 排序比较：双方可解析数值按 BigDecimal 比较，否则按字符串；不可比较（含 null）返回 null */
    private static Integer order(Object left, Object right) {
        if (left == null || right == null) {
            return null;
        }
        BigDecimal ln = toNumber(left);
        BigDecimal rn = toNumber(right);
        if (ln != null && rn != null) {
            return ln.compareTo(rn);
        }
        return str(left).compareTo(str(right));
    }

    /** Groovy truthiness：null/空串/数值 0/空集合/空 Map/false 为假 */
    public static boolean truthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return !((Map<?, ?>) value).isEmpty();
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString()).compareTo(BigDecimal.ZERO) != 0;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return true;
    }

    private static BigDecimal toNumber(Object value) {
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
