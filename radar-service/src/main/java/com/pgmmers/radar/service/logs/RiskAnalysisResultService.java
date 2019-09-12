package com.pgmmers.radar.service.logs;


import com.pgmmers.radar.vo.logs.RiskAnalysisResultVO;

/**
 * 风险分析数据处理服务.
 */
public interface RiskAnalysisResultService {

    void send(String guid, String sessionId, String result);
    
    int save(RiskAnalysisResultVO resp);
}
