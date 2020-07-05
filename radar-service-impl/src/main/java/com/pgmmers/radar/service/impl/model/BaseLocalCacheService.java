package com.pgmmers.radar.service.impl.model;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 围绕规则模型的缓存服务
 */
public abstract class BaseLocalCacheService implements SubscribeHandle {

    private static final Logger logger = LoggerFactory.getLogger(BaseLocalCacheService.class);
    protected LoadingCache<Long, Object> localCache = Caffeine
            .newBuilder()
            .maximumSize(64)
            .expireAfterAccess(Duration.ofHours(1))
            .build(this::query);

    public Object query(Long key) {
        logger.error("query is not implements,check!");
        return null;
    }

    public Object getByCache(Long key) {
        return localCache.get(key);
    }

    public void invalidateCache(Long key) {
        localCache.invalidate(key);
    }

}
