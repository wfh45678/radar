package com.pgmmers.radar.controller.engine;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 事件信息。
 * @author feihu.wang
 */
public class EventRequest {
    @Schema(description = "模型guid")
    @NotBlank(message = "guid 不能为空")
    private String guid;

    @Schema(description = "请求流水号")
    @NotBlank(message = "reqId 不能为空")
    private String reqId;

    @Schema(description = "事件内容")
    @NotNull(message = "jsonInfo 不能为空")
    private JSONObject jsonInfo;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public JSONObject getJsonInfo() {
        return jsonInfo;
    }

    public void setJsonInfo(JSONObject jsonInfo) {
        this.jsonInfo = jsonInfo;
    }
}
