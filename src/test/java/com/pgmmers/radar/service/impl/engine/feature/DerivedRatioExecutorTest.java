package com.pgmmers.radar.service.impl.engine.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.service.engine.feature.FeatureOutcome;
import com.pgmmers.radar.service.engine.feature.ExpressionEvaluator;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * L1 纯单测：衍生表达式求值器 + 衍生/组合比率执行器（v4.1 特征 SPI）。
 */
public class DerivedRatioExecutorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator(List.of());
    private final DerivedExecutor derivedExecutor = new DerivedExecutor();
    private final RatioExecutor ratioExecutor = new RatioExecutor();

    {
        org.springframework.test.util.ReflectionTestUtils.setField(derivedExecutor, "expressionEvaluator", evaluator);
    }
    private Map<String, Map<String, ?>> data;

    @BeforeEach
    void setUp() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("amount", 500.0);
        fields.put("mobile", "1851234");
        Map<String, Object> features = new HashMap<>();
        features.put("amt_1_day_sum", 9800.0);
        features.put("amt_1_day_qty", 8);
        features.put("ipInfo", Map.of("country", "中国"));
        data = new HashMap<>();
        data.put("fields", fields);
        data.put("features", features);
    }

    private FeatureVO derived(String expressionJson) throws Exception {
        FeatureVO feature = new FeatureVO();
        feature.setFeatureName("derived_1");
        feature.setType("DERIVED");
        feature.setDefinition(MAPPER.readTree("{\"expression\": " + expressionJson + "}"));
        return feature;
    }

    private FeatureVO ratio(String left, String right, String operator, String onZero) throws Exception {
        FeatureVO feature = new FeatureVO();
        feature.setFeatureName("ratio_1");
        feature.setType("RATIO");
        feature.setDefinition(MAPPER.readTree("{\"left\":\"" + left + "\",\"right\":\"" + right
                + "\",\"operator\":\"" + operator + "\",\"scale\":2,\"onZero\":\"" + onZero + "\"}"));
        return feature;
    }

    @Test
    @DisplayName("算术：ADD/DIV（除零 null）与嵌套列")
    void arithmetic() throws Exception {
        FeatureOutcome outcome = derivedExecutor.execute(derived(
                "{\"fn\":\"ADD\",\"args\":[{\"column\":\"features.amt_1_day_sum\"},{\"column\":\"fields.amount\"}]}"), data, Map.of());
        assertEquals(0, new BigDecimal("10300").compareTo((BigDecimal) outcome.getValue()));

        outcome = derivedExecutor.execute(derived(
                "{\"fn\":\"DIV\",\"args\":[{\"column\":\"features.amt_1_day_sum\"},{\"column\":\"fields.amount\"}]}"), data, Map.of());
        assertEquals(0, new BigDecimal("19.6").compareTo((BigDecimal) outcome.getValue()));

        // 除零：DIV 返回 null（执行器转无产出）
        Map<String, Object> fields0 = new HashMap<>(data.get("fields"));
        fields0.put("amount", 0);
        data.put("fields", fields0);
        outcome = derivedExecutor.execute(derived(
                "{\"fn\":\"DIV\",\"args\":[{\"column\":\"features.amt_1_day_sum\"},{\"column\":\"fields.amount\"}]}"), data, Map.of());
        assertNull(outcome.getValue());
        assertFalse(outcome.isPresent());
    }

    @Test
    @DisplayName("字符串：CONCAT/UPPER/REGEX_EXTRACT/SUBSTRING")
    void stringFunctions() throws Exception {
        FeatureOutcome outcome = derivedExecutor.execute(derived(
                "{\"fn\":\"CONCAT\",\"args\":[{\"column\":\"fields.mobile\"},{\"const\":\"_cn\"},{\"column\":\"features.ipInfo.country\"}]}"), data, Map.of());
        assertEquals("1851234_cn中国", outcome.getValue());

        outcome = derivedExecutor.execute(derived(
                "{\"fn\":\"UPPER\",\"args\":[{\"column\":\"fields.mobile\"}]}"), data, Map.of());
        assertEquals("1851234", outcome.getValue());

        outcome = derivedExecutor.execute(derived(
                "{\"fn\":\"REGEX_EXTRACT\",\"args\":[{\"column\":\"fields.mobile\"},{\"const\":\"^185\"}]}"), data, Map.of());
        assertEquals("185", outcome.getValue());

        outcome = derivedExecutor.execute(derived(
                "{\"fn\":\"SUBSTRING\",\"args\":[{\"column\":\"fields.mobile\"},{\"const\":\"0\"},{\"const\":\"3\"}]}"), data, Map.of());
        assertEquals("185", outcome.getValue());
    }

    @Test
    @DisplayName("MAPPING 映射表 + 缺省值；COALESCE 空值兜底")
    void mappingAndCoalesce() throws Exception {
        FeatureOutcome outcome = derivedExecutor.execute(derived(
                "{\"fn\":\"MAPPING\",\"args\":[{\"column\":\"fields.channel\"},{\"const\":\"a\"},{\"const\":\"1\"},{\"const\":\"b\"},{\"const\":\"2\"},{\"const\":\"0\"}]}"), data, Map.of());
        // channel 不在上下文 → null → 缺省值 0
        assertEquals("0", outcome.getValue());

        outcome = derivedExecutor.execute(derived(
                "{\"fn\":\"COALESCE\",\"args\":[{\"column\":\"fields.notExist\"},{\"const\":\"9\"}]}"), data, Map.of());
        assertEquals("9", outcome.getValue());
    }

    @Test
    @DisplayName("RATIO：金额/笔数=客单价；除零策略 ZERO/NULL")
    void ratio() throws Exception {
        FeatureOutcome outcome = ratioExecutor.execute(
                ratio("features.amt_1_day_sum", "features.amt_1_day_qty", "DIV", "ZERO"), data, Map.of());
        assertEquals(0, new BigDecimal("1225.00").compareTo((BigDecimal) outcome.getValue()));

        // 分母为 0：ZERO 策略填 0
        Map<String, Object> features = new HashMap<>(data.get("features"));
        features.put("amt_1_day_qty", 0);
        data.put("features", features);
        outcome = ratioExecutor.execute(
                ratio("features.amt_1_day_sum", "features.amt_1_day_qty", "DIV", "ZERO"), data, Map.of());
        assertEquals(0, BigDecimal.ZERO.compareTo((BigDecimal) outcome.getValue()));

        // NULL 策略：无产出
        outcome = ratioExecutor.execute(
                ratio("features.amt_1_day_sum", "features.amt_1_day_qty", "DIV", "NULL"), data, Map.of());
        assertFalse(outcome.isPresent());

        // SUB：差额特征（fields.amount=500 → 9800-500）
        outcome = ratioExecutor.execute(
                ratio("features.amt_1_day_sum", "fields.amount", "SUB", "ZERO"), data, Map.of());
        assertEquals(0, new BigDecimal("9300").compareTo((BigDecimal) outcome.getValue()));
    }

    @Test
    @DisplayName("未知函数/参数缺失：无产出不抛异常")
    void unknownFunctionTolerated() throws Exception {
        FeatureOutcome outcome = derivedExecutor.execute(derived(
                "{\"fn\":\"NO_SUCH_FN\",\"args\":[{\"const\":\"1\"}]}"), data, Map.of());
        assertNull(outcome.getValue());

        FeatureVO broken = new FeatureVO();
        broken.setFeatureName("broken");
        broken.setType("DERIVED");
        broken.setDefinition(MAPPER.readTree("{}"));
        outcome = derivedExecutor.execute(broken, data, Map.of());
        assertFalse(outcome.isPresent());
    }
}
