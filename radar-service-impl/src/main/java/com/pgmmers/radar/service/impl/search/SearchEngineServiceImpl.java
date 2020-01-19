package com.pgmmers.radar.service.impl.search;

import com.pgmmers.radar.service.search.SearchEngineService;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.Map;

@Service
public class SearchEngineServiceImpl implements SearchEngineService {
    public static Logger logger = LoggerFactory
            .getLogger(SearchEngineServiceImpl.class);

    @Autowired
    private TransportClient client;


    @Override
    public SearchHits search(String index, String type, String queryJson, String filterJson, Integer offset, Integer limit) {
        SearchResponse response = null;
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFrom(offset).setSize(limit).setExplain(true);
        if (!StringUtils.isEmpty(queryJson)) {
            builder.setQuery(QueryBuilders.queryStringQuery(queryJson));
        }
        if (!StringUtils.isEmpty(filterJson)) {
            builder.setPostFilter(QueryBuilders.queryStringQuery(filterJson));
        }

        response = builder.execute().actionGet();
        RestStatus status = response.status();
        if (status.equals(RestStatus.OK)) {
            SearchHits hits = response.getHits();
            return hits;
        } else {
            return null;
        }
    }

    @Override
    public SearchHits search(String index, String type, Map<String, Object> queryMap, Map<String, Object> filterMap, Integer offset, Integer limit) {
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

        return search(index, type, query, filter, offset, limit);
    }

    @Override
    public SearchHits search(String index, String type, QueryBuilder query, QueryBuilder filter, Integer offset, Integer limit) {
        SearchResponse response = null;
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFrom(offset).setSize(limit).setExplain(true);
        if (query != null) {
            builder.setQuery(query);
        }
        if (filter != null) {
            builder.setPostFilter(filter);
        }

        response = builder.execute().actionGet();
        RestStatus status = response.status();
        if (status.equals(RestStatus.OK)) {
            SearchHits hits = response.getHits();
            return hits;
        } else {
            return null;
        }
    }

    @Override
    public Long count(String index, String type, String query, String filter) {
        return 0L;
    }

    @Override
    public Long count(String index, String type, QueryBuilder query, QueryBuilder filter) {
        SearchResponse response = null;
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setSize(0).setExplain(true);
        if (!StringUtils.isEmpty(query)) {
            builder.setQuery(query);
        }
        if (!StringUtils.isEmpty(filter)) {
            builder.setPostFilter(filter);
        }
        response = builder.execute().actionGet();
        RestStatus status = response.status();
        if (status.equals(RestStatus.OK)) {
            SearchHits hits = response.getHits();
            return hits.getTotalHits();
        } else {
            return null;
        }
    }

    @Override
    public double avg(String index, String type, String field, String filter) {
        return 0;
    }

    @Override
    public double max(String index, String type, String field, String filter) {
        return 0;
    }

    @Override
    public Terms aggregateByTerms(String index, String type, String field, String filter) {
        return null;
    }

    @Override
    public boolean createIndex(String index, String indexAlias, String type, String jsonMapping) {
        IndicesAdminClient adminClient = client.admin().indices();
        DeleteIndexRequest request = Requests.deleteIndexRequest(index);
        adminClient.delete(request);
        adminClient.prepareCreate(index).get();
        AcknowledgedResponse resp;
        PutMappingRequestBuilder builder = adminClient.preparePutMapping(index);
        if (!StringUtils.isEmpty(jsonMapping)) {
            builder.setType(type).setSource(jsonMapping, XContentType.JSON);
        } else {
            return false;
        }
        resp = builder.get();
        if (resp.isAcknowledged()) {
            // 创建别名
            AcknowledgedResponse aliasResp = adminClient.prepareAliases().addAlias(index, indexAlias).get();
            if (aliasResp.isAcknowledged()) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

}
