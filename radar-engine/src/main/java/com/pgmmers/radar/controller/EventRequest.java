package com.pgmmers.radar.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 事件信息。
 * @author feihu.wang
 */
public class EventRequest {
    @ApiModelProperty(value = "模型guid")
    @NotBlank(message = "guid 不能为空")
    private String guid;

    @ApiModelProperty(value = "请求流水号")
    @NotBlank(message = "reqId 不能为空")
    private String reqId;

    @ApiModelProperty(value = "事件内容")
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
