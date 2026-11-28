package com.pgmmers.radar.service.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.io.IOException;
import java.util.Map;

/**
 * 风险事后数据搜索服务（ES 8，elasticsearch-java 客户端），用于事后分析。
 * v5.2 ES 8 升级：接口不再泄漏 7.x RestHighLevelClient 类型，
 * 查询构造统一使用 co.elastic.clients {@link Query} DSL（调用方经 Query.of 构建）。
 */
public interface SearchEngineService {

    /**
     * 事件检索分页（queryMap/filterMap 形态：entityId/fields 术语 + 时间范围）。
     */
    SearchResult search(String index, Map<String, Object> queryMap, Map<String, Object> filterMap,
                        Integer offset, Integer limit) throws IOException;

    /**
     * 事件检索分页（DSL Query 直传 + post filter）。
     */
    SearchResult search(String index, Query query, Query filter, Integer offset, Integer limit) throws IOException;

    /**
     * 命中计数（post filter 语义经 search size=0 保留）。
     */
    long count(String index, Query query, Query filter) throws IOException;

    boolean indexExist(String index) throws IOException;

    boolean createIndex(String index, String indexAlias, String jsonMapping) throws IOException;

    void deleteIndex(String index) throws IOException;
}
