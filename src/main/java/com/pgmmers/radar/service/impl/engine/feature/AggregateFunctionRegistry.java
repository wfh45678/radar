package com.pgmmers.radar.service.impl.engine.feature;

import com.pgmmers.radar.enums.AggregateKind;
import com.pgmmers.radar.service.engine.feature.AggregateFunction;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 聚合函数注册表（v4.1）：收集全部 {@link AggregateFunction} bean，按 {@link AggregateKind} 分发，
 * 替换 v3.x AggregateCommandImpl 的 switch-case。扩展新统计 = 枚举值 + 新 bean。
 */
@Component
public class AggregateFunctionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(AggregateFunctionRegistry.class);

    private final Map<AggregateKind, AggregateFunction> functions = new EnumMap<>(AggregateKind.class);

    public AggregateFunctionRegistry(List<AggregateFunction> functionList) {
        for (AggregateFunction function : functionList) {
            AggregateFunction prev = functions.putIfAbsent(function.kind(), function);
            if (prev != null) {
                logger.warn("duplicate AggregateFunction for kind {}: keep {}, ignore {}",
                        function.kind(), prev.getClass().getSimpleName(), function.getClass().getSimpleName());
            }
        }
        logger.info("aggregate function registry initialized: {}", functions.keySet());
    }

    /** 按统计类型取函数；未注册返回 null */
    public AggregateFunction get(AggregateKind kind) {
        return functions.get(kind);
    }
}
