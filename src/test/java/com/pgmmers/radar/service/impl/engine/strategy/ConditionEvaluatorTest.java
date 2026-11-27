package com.pgmmers.radar.service.impl.engine.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmmers.radar.service.engine.strategy.ConditionEvaluator;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * L1 纯单测：后端条件求值器（ConditionEvaluator）。
 * 覆盖 20 个操作符 + PDCT 四种 linking + truthiness + 名单 + 缺值语义，
 * 语义基线为前端 Groovy 编译器（frontend/src/component/utils/groovyUtil.jsx）。
 */
class ConditionEvaluatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Map<String, Map<String, ?>> data;
    private Map<String, Object> lists;

    @BeforeEach
    void setUp() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("amount", 250.5);
        fields.put("cardNo", "6222020200");
        fields.put("channel", "app");
        fields.put("age", 20);
        fields.put("emptyStr", "");
        fields.put("zero", 0);

        Map<String, Object> preItems = new HashMap<>();
        preItems.put("ipProvince", "浙江省");

        Map<String, Object> abstractions = new HashMap<>();
        abstractions.put("tran_ip_1_day_qty", 35);
        abstractions.put("amt_1_day_sum", 9800.0);

        data = new HashMap<>();
        data.put("fields", fields);
        data.put("preItems", preItems);
        data.put("abstractions", abstractions);

        Map<String, String> blackIp = new HashMap<>();
        blackIp.put("1.2.3.4", "");
        blackIp.put("5.6.7.8", "");
        lists = new HashMap<>();
        lists.put("black_ip", blackIp);
    }

    private boolean eval(String definitionJson) {
        try {
            JsonNode node = MAPPER.readTree(definitionJson);
            return ConditionEvaluator.evaluate(node, data, lists);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String smpl(String operator, String leftColumn, String leftType,
                               String value, String valueType) {
        return "{\"class\":\"SMPL\",\"enabled\":true,\"operator\":\"" + operator + "\","
                + "\"expressions\":["
                + "{\"class\":\"ENTATTR\",\"type\":\"" + leftType + "\",\"column\":\"" + leftColumn + "\"},"
                + "{\"class\":\"CONST\",\"type\":\"" + valueType + "\",\"value\":\"" + value + "\"}]}";
    }

    @Test
    @DisplayName("数值排序操作符：Greater/Less 及边界（>=30 命中 35）")
    void numericOrdering() {
        assertTrue(eval(smpl("Greater_Equal", "abstractions.tran_ip_1_day_qty", "DOUBLE", "30", "DOUBLE")));
        assertTrue(eval(smpl("Greater", "abstractions.tran_ip_1_day_qty", "DOUBLE", "34.9", "DOUBLE")));
        assertFalse(eval(smpl("Greater", "abstractions.tran_ip_1_day_qty", "DOUBLE", "35", "DOUBLE")));
        assertTrue(eval(smpl("Less", "fields.age", "INTEGER", "65", "INTEGER")));
        assertTrue(eval(smpl("Less_Equal", "fields.age", "INTEGER", "20", "INTEGER")));
        assertFalse(eval(smpl("Less_Equal", "fields.age", "INTEGER", "19", "INTEGER")));
    }

    @Test
    @DisplayName("等值/不等：数值形态与字符串形态各一")
    void equality() {
        assertTrue(eval(smpl("Equal", "fields.amount", "DOUBLE", "250.5", "DOUBLE")));
        assertTrue(eval(smpl("Equal", "fields.channel", "STRING", "app", "STRING")));
        assertFalse(eval(smpl("Equal", "fields.channel", "STRING", "web", "STRING")));
        assertTrue(eval(smpl("NotEqual", "fields.channel", "STRING", "web", "STRING")));
    }

    @Test
    @DisplayName("字符串包含/前缀")
    void stringOps() {
        assertTrue(eval(smpl("StartsWith", "fields.cardNo", "STRING", "6222", "STRING")));
        assertFalse(eval(smpl("NotStartsWith", "fields.cardNo", "STRING", "6222", "STRING")));
        assertTrue(eval(smpl("Contains", "fields.cardNo", "STRING", "0200", "STRING")));
        assertTrue(eval(smpl("NotContains", "fields.cardNo", "STRING", "9999", "STRING")));
    }

    @Test
    @DisplayName("名单操作符：InList 命中 / NotInList 未命中；名单缺失语义")
    void listOps() {
        // fields.ip 未放入上下文 → 取值为 null → InList 为 false
        assertFalse(eval(smpl("InList", "fields.ip", "STRING", "black_ip", "LIST")));
        Map<String, Object> fields = new HashMap<>(data.get("fields"));
        fields.put("ip", "5.6.7.8");
        data.put("fields", fields);
        assertTrue(eval(smpl("InList", "fields.ip", "STRING", "black_ip", "LIST")));
        assertTrue(eval(smpl("NotInList", "fields.ip", "STRING", "white_ip", "LIST")));
        // 名单缺失：InList=false，NotInList=true（区别于 Groovy 整体报 false 的旧行为，语义更稳）
        lists.remove("black_ip");
        assertFalse(eval(smpl("InList", "fields.ip", "STRING", "black_ip", "LIST")));
        assertTrue(eval(smpl("NotInList", "fields.ip", "STRING", "black_ip", "LIST")));
    }

    @Test
    @DisplayName("IsNotNull/IsNull：Groovy truthiness（空串、0 视为空）")
    void nullOps() {
        assertTrue(eval("{\"class\":\"SMPL\",\"operator\":\"IsNotNull\",\"expressions\":[{\"class\":\"ENTATTR\",\"column\":\"fields.amount\"}]}"));
        assertFalse(eval("{\"class\":\"SMPL\",\"operator\":\"IsNotNull\",\"expressions\":[{\"class\":\"ENTATTR\",\"column\":\"fields.emptyStr\"}]}"));
        assertFalse(eval("{\"class\":\"SMPL\",\"operator\":\"IsNotNull\",\"expressions\":[{\"class\":\"ENTATTR\",\"column\":\"fields.zero\"}]}"));
        assertTrue(eval("{\"class\":\"SMPL\",\"operator\":\"IsNull\",\"expressions\":[{\"class\":\"ENTATTR\",\"column\":\"fields.notExist\"}]}"));
        assertTrue(eval("{\"class\":\"SMPL\",\"operator\":\"IsNotNull\",\"expressions\":[{\"class\":\"ENTATTR\",\"column\":\"preItems.ipProvince\"}]}"));
    }

    @Test
    @DisplayName("Field_ 前缀：字段对字段比较")
    void fieldOps() {
        assertTrue(eval("{\"class\":\"SMPL\",\"operator\":\"Field_Greater\",\"expressions\":["
                + "{\"class\":\"ENTATTR\",\"column\":\"abstractions.amt_1_day_sum\"},"
                + "{\"class\":\"ENTATTR\",\"column\":\"fields.amount\"}]}"));
        assertTrue(eval("{\"class\":\"SMPL\",\"operator\":\"Field_Equal\",\"expressions\":["
                + "{\"class\":\"ENTATTR\",\"column\":\"fields.age\"},"
                + "{\"class\":\"ENTATTR\",\"column\":\"fields.age\"}]}"));
        assertFalse(eval("{\"class\":\"SMPL\",\"operator\":\"Field_Not_Equal\",\"expressions\":["
                + "{\"class\":\"ENTATTR\",\"column\":\"fields.age\"},"
                + "{\"class\":\"ENTATTR\",\"column\":\"fields.age\"}]}"));
    }

    @Test
    @DisplayName("PDCT linking：All/Any/NotAll/None（对齐前端生成语义）")
    void compositeLinking() {
        String ge30 = smpl("Greater_Equal", "abstractions.tran_ip_1_day_qty", "DOUBLE", "30", "DOUBLE");
        String le25 = smpl("Less_Equal", "fields.age", "INTEGER", "25", "INTEGER");
        String eqWeb = smpl("Equal", "fields.channel", "STRING", "web", "STRING");

        assertTrue(eval("{\"class\":\"PDCT\",\"linking\":\"All\",\"conditions\":[" + ge30 + "," + le25 + "]}"));
        assertFalse(eval("{\"class\":\"PDCT\",\"linking\":\"All\",\"conditions\":[" + ge30 + "," + eqWeb + "]}"));
        assertTrue(eval("{\"class\":\"PDCT\",\"linking\":\"Any\",\"conditions\":[" + eqWeb + "," + le25 + "]}"));
        // NotAll = !(全部成立)：一个不成立则为 true
        assertTrue(eval("{\"class\":\"PDCT\",\"linking\":\"NotAll\",\"conditions\":[" + ge30 + "," + eqWeb + "]}"));
        assertFalse(eval("{\"class\":\"PDCT\",\"linking\":\"NotAll\",\"conditions\":[" + ge30 + "," + le25 + "]}"));
        // None = 全不成立
        assertTrue(eval("{\"class\":\"PDCT\",\"linking\":\"None\",\"conditions\":[" + eqWeb + ","
                + smpl("Less", "fields.age", "INTEGER", "18", "INTEGER") + "]}"));
        assertFalse(eval("{\"class\":\"PDCT\",\"linking\":\"None\",\"conditions\":[" + eqWeb + "," + le25 + "]}"));
    }

    @Test
    @DisplayName("PDCT 嵌套 PDCT")
    void nestedComposite() {
        String inner = "{\"class\":\"PDCT\",\"linking\":\"Any\",\"conditions\":["
                + smpl("Equal", "fields.channel", "STRING", "app", "STRING") + ","
                + smpl("Equal", "fields.channel", "STRING", "h5", "STRING") + "]}";
        String outer = "{\"class\":\"PDCT\",\"linking\":\"All\",\"conditions\":[" + inner + ","
                + smpl("Greater", "fields.amount", "DOUBLE", "100", "DOUBLE") + "]}";
        assertTrue(eval(outer));
    }

    @Test
    @DisplayName("缺值语义：排序/等值单侧 null 为 false，双 null 相等")
    void missingValueSemantics() {
        assertFalse(eval(smpl("Greater", "fields.notExist", "DOUBLE", "1", "DOUBLE")));
        assertFalse(eval(smpl("Less_Equal", "fields.notExist", "DOUBLE", "1", "DOUBLE")));
        assertFalse(eval(smpl("Equal", "fields.notExist", "STRING", "x", "STRING")));
        assertTrue(eval(smpl("Equal", "fields.notExist", "STRING", "", "STRING") // null vs 空串 → 不等
                ) == false);
        // 非数值字段回退字符串排序
        assertTrue(eval(smpl("Greater", "preItems.ipProvince", "STRING", "江苏", "STRING")));
    }

    @Test
    @DisplayName("definition 为空按 false 处理")
    void emptyDefinition() throws Exception {
        assertFalse(ConditionEvaluator.evaluate(null, data, lists));
        assertFalse(ConditionEvaluator.evaluate(MAPPER.readTree("null"), data, lists));
        assertFalse(ConditionEvaluator.evaluate(MAPPER.readTree("{}"), data, lists));
    }

    @Test
    @DisplayName("resolveColumn：点号路径 + 无点号回退 abstractions/fields")
    void resolveColumnFallback() {
        assertEquals(35, ((Number) ConditionEvaluator.resolveColumn("abstractions.tran_ip_1_day_qty", data)).intValue());
        assertEquals("app", ConditionEvaluator.resolveColumn("fields.channel", data));
        // 无点号：先 abstractions 后 fields
        assertEquals(35, ((Number) ConditionEvaluator.resolveColumn("tran_ip_1_day_qty", data)).intValue());
        assertEquals("app", ConditionEvaluator.resolveColumn("channel", data));
    }
}
