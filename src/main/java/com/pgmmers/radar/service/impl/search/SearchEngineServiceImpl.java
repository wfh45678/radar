package com.pgmmers.radar.service.impl.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson2.JSON;
import com.pgmmers.radar.service.search.SearchEngineService;
import com.pgmmers.radar.service.search.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 检索/建索引实现（v5.2 ES 8 升级）：elasticsearch-java 客户端 lambda DSL，
 * 替换已停止演进的 7.x RestHighLevelClient。结果以 JSON 字符串形态回传，
 * 下游 fastjson2 解析链路保持不变。
 */
@Service
public class SearchEngineServiceImpl implements SearchEngineService {

    private static final Logger logger = LoggerFactory.getLogger(SearchEngineServiceImpl.class);

    @Autowired
    private ElasticsearchClient client;

    @Override
    public SearchResult search(String index, Map<String, Object> queryMap, Map<String, Object> filterMap,
                               Integer offset, Integer limit) throws IOException {
        Query query = null;
        Query filter = null;
        if (queryMap != null) {
            Object entityId = queryMap.get("entityId");
            if (!StringUtils.isEmpty(entityId)) {
                String fieldName = "fields." + queryMap.get("entityName").toString();
                String value = entityId.toString();
                query = Query.of(q -> q.term(t -> t.field(fieldName).value(v -> v.stringValue(value))));
            }
        }
        if (filterMap != null) {
            Long beginTime = (Long) filterMap.get("beginTime");
            Long endTime = (Long) filterMap.get("endTime");
            String rangeField = "fields." + filterMap.get("refDate").toString();
            filter = Query.of(q -> q.range(r -> r.untyped(u -> u
                    .gte(JsonData.of(beginTime))
                    .lte(JsonData.of(endTime))
                    .field(rangeField))));
        }
        return search(index, query, filter, offset, limit);
    }

    @Override
    public SearchResult search(String index, Query query, Query filter, Integer offset, Integer limit)
            throws IOException {
        logger.debug("search, index={}, offset:{}, limit:{}", index, offset, limit);
        SearchResponse<JsonData> response = client.search(s -> {
            s.index(index).from(offset).size(limit);
            if (query != null) {
                s.query(query);
            }
            if (filter != null) {
                s.postFilter(filter);
            }
            return s;
        }, JsonData.class);
        return toResult(response);
    }

    @Override
    public long count(String index, Query query, Query filter) throws IOException {
        // count API 不支持 post_filter，走 search size=0 保留过滤语义（与 7.x 行为一致）
        SearchResponse<JsonData> response = client.search(s -> {
            s.index(index).size(0);
            if (query != null) {
                s.query(query);
            }
            if (filter != null) {
                s.postFilter(filter);
            }
            return s;
        }, JsonData.class);
        if (response.hits().total() != null) {
            return response.hits().total().value();
        }
        return 0L;
    }

    @Override
    public boolean indexExist(String index) throws IOException {
        return client.indices().exists(e -> e.index(index)).value();
    }

    @Override
    public boolean createIndex(String index, String indexAlias, String jsonMapping) throws IOException {
        boolean exist = this.indexExist(index);
        if (exist) {
            // 先删后建（场景构建时全量重建 mapping，事件数据可由 Mongo 重建）
            this.deleteIndex(index);
        }
        client.indices().create(c -> c
                .index(index)
                .aliases(indexAlias, a -> a)
                .mappings(m -> m.withJson(new StringReader(jsonMapping))));
        return true;
    }

    @Override
    public void deleteIndex(String index) throws IOException {
        client.indices().delete(d -> d.index(index));
    }

    private SearchResult toResult(SearchResponse<JsonData> response) {
        long total = response.hits().total() != null ? response.hits().total().value() : 0;
        List<String> sources = new ArrayList<>();
        for (var hit : response.hits().hits()) {
            JsonData source = hit.source();
            sources.add(source == null ? null : source.toJson().toString());
        }
        return new SearchResult(total, sources);
    }
}
