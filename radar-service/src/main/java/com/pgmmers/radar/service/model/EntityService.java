package com.pgmmers.radar.service.model;

import java.util.Date;

public interface EntityService {

    Date save(Long modelId, String jsonString, String attachJson, boolean isAllowDuplicate);

}
