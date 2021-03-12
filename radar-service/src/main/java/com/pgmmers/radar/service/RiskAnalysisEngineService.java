package com.pgmmers.radar.service;


import com.alibaba.fastjson.JSONObject;
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
     * @param modelGuid string of model id
     * @param reqId req id
     * @param jsonInfo json string of transaction
     * @return result vo {@link CommonResult}
     * @author feihu.wang
     *
     */
     CommonResult uploadInfo(String modelGuid, String reqId, String jsonInfo);

    /**
     * 上传信息.
     * @param modelGuid
     * @param reqId
     * @param jsonInfo
     * @return
     * @see "uploadInfo(String modelGuid, String reqId, String jsonInfo)"
     */
     CommonResult uploadInfo(String modelGuid, String reqId, JSONObject jsonInfo);

    /**
     * 
     * 查询分析结果.
     * 
     * @param modelGuid modelGuid
     * @param reqId req id
     * @return result vo {@link CommonResult}
     * @author feihu.wang
     *
     */

     CommonResult getScore(String modelGuid, String reqId);

}
