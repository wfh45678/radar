package com.pgmmers.radar.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.Map;


/**
 * 
 * 风险事后数据搜索服务（ES）, 用于事后分析。
 * @author feihu.wang
 */
public interface SearchEngineService {

     SearchHits search(String index, String type, String queryJson, String filterJson, Integer offset, Integer limit);
    
     SearchHits search(String index, String type, Map<String, Object> queryMap, Map<String, Object> filterMap, Integer offset, Integer limit);
    
     SearchHits search(String index, String type, QueryBuilder query, QueryBuilder filter, Integer offset, Integer limit);
            
     Long count(String index, String type, String query, String filter);

     Long count(String index, String type, QueryBuilder query, QueryBuilder filter);

     double avg(String index, String type, String field, String filter);
    
     double max(String index, String type, String field, String filter);
    
    
     Terms aggregateByTerms(String index, String type, String field, String filter);
    
     boolean createIndex(String index, String indexAlias, String type, String jsonMapping);
    
    
}
