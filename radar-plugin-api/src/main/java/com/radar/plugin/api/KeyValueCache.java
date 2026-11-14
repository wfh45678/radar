package com.radar.plugin.api;

import java.util.function.Function;

/**
 * 插件结果缓存（引擎实现，底层为进程内 TTL 缓存）。
 * 归属地查询、外部数据服务结果等天然冷热分明的数据建议接入，可大幅降低外部调用量。
 *
 * @since 3.0.0
 */
public interface KeyValueCache {

    /**
     * 取值，不存在时用 loader 计算并放入缓存（并发下同一 key 只加载一次）。
     * loader 返回 null 表示不缓存。
     */
    <K, V> V get(K key, Function<? super K, ? extends V> loader);
}
