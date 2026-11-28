package com.pgmmers.radar.service;


import com.alibaba.fastjson2.JSONObject;
import com.pgmmers.radar.service.common.CommonResult;


/**
 * 风险分析引擎主入口.
 * risk analysis engine api.
 * 
 * @author feihu.wang
 *
 */
public interface RiskAnalysisEngineService {

    /**
     * 
     * 上传信息.
     * 
     * @param scenarioGuid string of model id
     * @param reqId req id
     * @param jsonInfo json string of transaction
     * @return result vo {@link CommonResult}
     * @author feihu.wang
     *
     */
     CommonResult uploadInfo(String scenarioGuid, String reqId, String jsonInfo);

    /**
     * 上传信息.
     * @param scenarioGuid
     * @param reqId
     * @param jsonInfo
     * @return
     * @see "uploadInfo(String scenarioGuid, String reqId, String jsonInfo)"
     */
     CommonResult uploadInfo(String scenarioGuid, String reqId, JSONObject jsonInfo);

    /**
     * 
     * 查询分析结果.
     * 
     * @param scenarioGuid scenarioGuid
     * @param reqId req id
     * @return result vo {@link CommonResult}
     * @author feihu.wang
     *
     */
     CommonResult getScore(String scenarioGuid, String reqId);

    /**
     * sync event status.
     * @param scenarioGuid
     * @param eventId
     * @param status
     * @return
     */
     CommonResult syncStatus(String scenarioGuid, String eventId, String status);
}
