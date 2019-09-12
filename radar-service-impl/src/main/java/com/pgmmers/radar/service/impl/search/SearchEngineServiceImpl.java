package com.pgmmers.radar.service.impl.search;

import com.pgmmers.radar.service.search.SearchEngineService;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public class SearchEngineServiceImpl implements SearchEngineService {

    @Override
    public SearchHits search(String index, String type, String queryJson, String filterJson, Integer offset, Integer limit) {
        return null;
    }

    @Override
    public SearchHits search(String index, String type, Map<String, Object> queryMap, Map<String, Object> filterMap, Integer offset, Integer limit) {
        return null;
    }

    @Override
    public SearchHits search(String index, String type, QueryBuilder query, QueryBuilder filter, Integer offset, Integer limit) {
        return null;
    }

    @Override
    public Long count(String index, String type, String query, String filter) {
        return null;
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

        return true;
    }
}
