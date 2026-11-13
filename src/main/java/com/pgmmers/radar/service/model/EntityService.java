package com.pgmmers.radar.service.model;

import java.util.List;


public interface EntityService {

    int save(Long modelId, String jsonString, boolean isAllowDuplicate);
    
    int save(Long modelId, String jsonString, String attachJson, boolean isAllowDuplicate);
        
    List<Object> find(Long modelId, String conds);

    /**
     * update status.
     * @param modelId
     * @param eventId
     * @param status
     * @return
     */
    long update(Long modelId, String eventId, String status);

}
