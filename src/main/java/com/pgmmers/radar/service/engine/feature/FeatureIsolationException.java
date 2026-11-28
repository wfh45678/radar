package com.pgmmers.radar.service.engine.feature;

/**
 * 特征执行失败且隔离模式为 strict 时抛出（v4.1）：
 * 事件链路按旧行为整体失败；编排层对该异常不再做单特征隔离。
 */
public class FeatureIsolationException extends RuntimeException {

    public FeatureIsolationException(String message) {
        super(message);
    }

    public FeatureIsolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
