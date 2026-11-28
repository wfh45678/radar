package com.pgmmers.radar.service.impl.engine.feature;

import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.service.engine.feature.FeatureExecutor;
import com.pgmmers.radar.service.engine.feature.FeatureOutcome;
import com.pgmmers.radar.service.engine.strategy.ConditionEvaluator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 组合比率特征执行器（v4.1）：两个特征/字段的组合运算（DIV/SUB/ADD/MUL），
 * 主要面向统计特征二次加工（如 金额/笔数=客单价）。典型场景在 ANALYSIS 阶段
 * 引用 AGGREGATE 产出（阶段语义保证可用），亦可组合 ENTRY 特征或事件字段。
 *
 * <p>DEFINITION：{left: 列路径, right: 列路径, operator: DIV|SUB|ADD|MUL, scale: 精度(默认 2),
 * onZero: ZERO|NULL（DIV 除零策略，默认 ZERO）}。非数值/除零（NULL 策略）无产出。</p>
 */
@Component
public class RatioExecutor implements FeatureExecutor {

    @Override
    public FeatureType type() {
        return FeatureType.RATIO;
    }

    @Override
    public FeatureOutcome execute(FeatureVO feature, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        var definition = feature.getDefinition();
        if (definition == null || definition.isNull() || definition.isEmpty()) {
            return FeatureOutcome.empty();
        }
        Object left = ConditionEvaluator.resolveColumn(definition.path("left").asText(null), data);
        Object right = ConditionEvaluator.resolveColumn(definition.path("right").asText(null), data);
        BigDecimal a = toNumber(left);
        BigDecimal b = toNumber(right);
        if (a == null || b == null) {
            return FeatureOutcome.empty();
        }
        String operator = definition.path("operator").asText("DIV");
        int scale = definition.path("scale").asInt(2);
        boolean onZeroNull = "NULL".equalsIgnoreCase(definition.path("onZero").asText("ZERO"));

        BigDecimal result;
        switch (operator.toUpperCase()) {
        case "SUB":
            result = a.subtract(b);
            break;
        case "ADD":
            result = a.add(b);
            break;
        case "MUL":
            result = a.multiply(b);
            break;
        case "DIV":
        default:
            if (BigDecimal.ZERO.compareTo(b) == 0) {
                // 除零策略：ZERO（默认）填 0，NULL 无产出
                return onZeroNull ? FeatureOutcome.empty() : FeatureOutcome.of(BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP));
            }
            result = a.divide(b, Math.max(0, scale), RoundingMode.HALF_UP);
            break;
        }
        return FeatureOutcome.of(result);
    }

    private BigDecimal toNumber(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
