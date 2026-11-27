package com.pgmmers.radar.service.impl.engine.strategy;

import com.pgmmers.radar.enums.StrategyType;
import com.pgmmers.radar.service.engine.strategy.StrategyExecutor;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 策略执行器注册表（v4.0）：收集容器内全部 {@link StrategyExecutor} bean，
 * 按 {@link StrategyType} 分发。范式对齐 {@code PluginManager} 的注册表模式。
 *
 * <p>同类型重复注册时首个生效并告警（防 bean 冲突静默覆盖）；
 * 未注册类型返回 null，由编排层按策略隔离处理。</p>
 */
@Component
public class StrategyExecutorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(StrategyExecutorRegistry.class);

    private final Map<StrategyType, StrategyExecutor> executors = new EnumMap<>(StrategyType.class);

    public StrategyExecutorRegistry(List<StrategyExecutor> executorList) {
        for (StrategyExecutor executor : executorList) {
            StrategyExecutor prev = executors.putIfAbsent(executor.type(), executor);
            if (prev != null) {
                logger.warn("duplicate StrategyExecutor for type {}: keep {}, ignore {}",
                        executor.type(), prev.getClass().getSimpleName(), executor.getClass().getSimpleName());
            }
        }
        logger.info("strategy executor registry initialized: {}", executors.keySet());
    }

    /** 按类型取执行器；未注册返回 null */
    public StrategyExecutor get(StrategyType type) {
        return executors.get(type);
    }
}
