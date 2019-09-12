package com.pgmmers.radar.service.model;

import java.util.List;


public interface EntityService {

    public int save(Long modelId, String jsonString, boolean isAllowDuplicate);
    
    public int save(Long modelId, String jsonString, String attachJson, boolean isAllowDuplicate);
        
    public List<Object> find(Long modelId, String conds);

    


}
