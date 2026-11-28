package com.pgmmers.radar.service.data;

public interface RiskResultService {
    void sendResult(String scenarioGuid, String reqId, String info);
}
