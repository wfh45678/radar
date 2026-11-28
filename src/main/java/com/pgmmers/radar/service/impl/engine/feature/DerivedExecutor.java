package com.pgmmers.radar.service.impl.engine.feature;

import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.service.engine.feature.FeatureExecutor;
import com.pgmmers.radar.service.engine.feature.FeatureOutcome;
import com.pgmmers.radar.service.engine.feature.ExpressionEvaluator;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 衍生表达式特征执行器（v4.1）：解释 DEFINITION.expression AST——
 * 事件字段与既有特征的表达式运算（算术/字符串函数，见 {@code StandardDerivedFunctions}）。
 * 求值结果（BigDecimal/String）写入 features.&lt;featureName&gt;；求值失败/null 无产出。
 * ENTRY 阶段执行，可引用前序特征（ORDER_NO 顺序 + 阶段语义保证）。
 */
@Component
public class DerivedExecutor implements FeatureExecutor {

    @Autowired
    private ExpressionEvaluator expressionEvaluator;

    @Override
    public FeatureType type() {
        return FeatureType.DERIVED;
    }

    @Override
    public FeatureOutcome execute(FeatureVO feature, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        var expression = feature.getDefinition() == null ? null : feature.getDefinition().get("expression");
        if (expression == null || expression.isNull() || expression.isEmpty()) {
            return FeatureOutcome.empty();
        }
        Object value = expressionEvaluator.evaluate(expression, data);
        return FeatureOutcome.of(value);
    }
}
