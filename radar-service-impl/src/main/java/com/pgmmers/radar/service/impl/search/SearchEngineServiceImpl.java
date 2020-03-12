package com.pgmmers.radar.service.impl.search;

import com.alibaba.fastjson.JSON;
import com.pgmmers.radar.service.search.SearchEngineService;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

@Service
public class SearchEngineServiceImpl implements SearchEngineService {
    public static Logger logger = LoggerFactory
            .getLogger(SearchEngineServiceImpl.class);
    @Autowired
    private RestHighLevelClient client;

    public RestHighLevelClient getClient() {
        return client;
    }

    public void setClient(RestHighLevelClient client) {
        this.client = client;
    }

    @Override
    public SearchHits search(String index, String queryJson, String filterJson, Integer offset, Integer limit) throws IOException {

        SearchRequest request = new SearchRequest(index);
        request.searchType(SearchType.DFS_QUERY_THEN_FETCH);

        SearchSourceBuilder builder = new SearchSourceBuilder();

        builder
                .query(QueryBuilders.wrapperQuery(queryJson))
                .postFilter(QueryBuilders.wrapperQuery(filterJson))
//                .postFilter(QueryBuilders.matchQuery("type", type))
                .from(offset)
                .size(limit);
        request.source(builder);
        SearchResponse response =  client.search(request, RequestOptions.DEFAULT);
        RestStatus status = response.status();
        if (status.equals(RestStatus.OK)) {
            SearchHits hits = response.getHits();
            return hits;
        } else {
            return null;
        }
    }

    @Override
    public SearchHits search(String index, Map<String, Object> queryMap, Map<String, Object> filterMap, Integer offset, Integer limit) throws IOException {
        QueryBuilder query = null;
        QueryBuilder filter = null;
        if (queryMap != null) {
            Object entityId = queryMap.get("entityId");
            if (!StringUtils.isEmpty(entityId)) {
                query = QueryBuilders.termQuery(
                        "fields." + queryMap.get("entityName").toString(),
                        entityId.toString());
            }
        }
        if (filterMap != null) {
            Long beginTime = (Long) filterMap.get("beginTime");
            Long endTime = (Long) filterMap.get("endTime");
            filter = QueryBuilders
                    .rangeQuery("fields." + filterMap.get("refDate").toString())
                    .from(beginTime.longValue()).to(endTime.longValue());
        }

        return search(index, query, filter, offset, limit);
    }

    @Override
    public SearchHits search(String index, QueryBuilder query, QueryBuilder filter, Integer offset, Integer limit) throws IOException {
        logger.debug("search, index={}, query:{}, filter:{}, offset:{}, limit:{}", index, JSON.toJSONString(query), JSON.toJSONString(filter), offset, limit);
        SearchRequest request = new SearchRequest(index);
        request.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        SearchSourceBuilder builder = new SearchSourceBuilder();

        builder.query(query)
                .postFilter(filter)
//                .postFilter(QueryBuilders.matchQuery("type", type))
                .from(offset)
                .size(limit)
                .explain(true)
        ;
        request.source(builder);
        SearchResponse response =  client.search(request, RequestOptions.DEFAULT);
        logger.debug("search result: {}", JSON.toJSONString(response));
        RestStatus status = response.status();
        if (status.equals(RestStatus.OK)) {
            SearchHits hits = response.getHits();
            return hits;
        } else {
            return null;
        }
    }

    @Override
    public Long count(String index, String query, String filter) {
        return 0L;
    }

    @Override
    public Long count(String index, QueryBuilder query, QueryBuilder filter) throws IOException {
        SearchRequest request = new SearchRequest(index);
        request.searchType(SearchType.DFS_QUERY_THEN_FETCH);
//        request.types(type);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(query)
                .postFilter(filter)
//                .postFilter(QueryBuilders.matchQuery("type", type))
                .size(0)
                .explain(true)
        ;
        request.source(builder);
        SearchResponse response =  client.search(request, RequestOptions.DEFAULT);
        RestStatus status = response.status();
        if (status.equals(RestStatus.OK)) {
            SearchHits hits = response.getHits();
            return hits.getTotalHits().value;
        } else {
            return null;
        }
    }

    @Override
    public double avg(String index,String field, String filter) {
        return 0;
    }

    @Override
    public double max(String index, String field, String filter) {
        return 0;
    }

    @Override
    public Terms aggregateByTerms(String index, String field, String filter) {
        return null;
    }

    public boolean indexExist(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }

    @Override
    public boolean createIndex(String index, String indexAlias, String jsonMapping) throws IOException {
        boolean exist = this.indexExist(index);
        if(exist){
            this.deleteIndex(index);
        }
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.mapping(jsonMapping, XContentType.JSON);
        request.alias(new Alias(indexAlias));
        CreateIndexResponse resp = client.indices().create(request, RequestOptions.DEFAULT);

        if (resp.isAcknowledged()) {
            // 创建别名
//            AcknowledgedResponse aliasResp = adminClient.prepareAliases().addAlias(index, indexAlias).get();
//            if (aliasResp.isAcknowledged()) {
//                return true;
//            } else {
//                return false;
//            }
            return true;

        } else {
            return false;
        }
    }

    public void deleteIndex(String index) throws IOException {
        client.indices().delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);
    }


}
