package com.pgmmers.radar.service.impl.engine.feature;

import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.service.engine.feature.FeatureExecutor;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 特征执行器注册表（v4.1）：收集容器内全部 {@link FeatureExecutor} bean，
 * 按 {@link FeatureType} 分发。范式对齐 {@code StrategyExecutorRegistry} / {@code PluginManager}。
 */
@Component
public class FeatureExecutorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(FeatureExecutorRegistry.class);

    private final Map<FeatureType, FeatureExecutor> executors = new EnumMap<>(FeatureType.class);

    public FeatureExecutorRegistry(List<FeatureExecutor> executorList) {
        for (FeatureExecutor executor : executorList) {
            FeatureExecutor prev = executors.putIfAbsent(executor.type(), executor);
            if (prev != null) {
                logger.warn("duplicate FeatureExecutor for type {}: keep {}, ignore {}",
                        executor.type(), prev.getClass().getSimpleName(), executor.getClass().getSimpleName());
            }
        }
        logger.info("feature executor registry initialized: {}", executors.keySet());
    }

    /** 按类型取执行器；未注册返回 null（编排层按特征隔离跳过并告警） */
    public FeatureExecutor get(FeatureType type) {
        return executors.get(type);
    }
}
