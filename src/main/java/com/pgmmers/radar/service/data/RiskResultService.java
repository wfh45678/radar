package com.pgmmers.radar.service.data;

public interface RiskResultService {
    void sendResult(String modelGuid, String reqId, String info);
}
