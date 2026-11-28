package com.pgmmers.radar.service.engine;


import com.pgmmers.radar.model.vo.model.FieldVO;

import java.util.List;
import java.util.Map;

/**
 * 
 * 参数校验.
 * @author feihu.wang
 * @version Revision 0.0.1
 */
public interface ValidateService {

    public Map<String, Object> validate(Long scenarioId, Map data);
    
    public Map<String, Object> validate(List<FieldVO> fieldList, Map data);
    
}
