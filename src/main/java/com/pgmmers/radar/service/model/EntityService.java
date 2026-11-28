package com.pgmmers.radar.service.model;

import java.util.List;


public interface EntityService {

    int save(Long scenarioId, String jsonString, boolean isAllowDuplicate);
    
    int save(Long scenarioId, String jsonString, String attachJson, boolean isAllowDuplicate);
        
    List<Object> find(Long scenarioId, String conds);

    /**
     * update status.
     * @param scenarioId
     * @param eventId
     * @param status
     * @return
     */
    long update(Long scenarioId, String eventId, String status);

}
