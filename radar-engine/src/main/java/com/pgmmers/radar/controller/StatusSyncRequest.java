package com.pgmmers.radar.controller;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 状态同步请求。
 * @author feihu.wang
 */
public class StatusSyncRequest {
    @ApiModelProperty(value = "模型guid")
    @NotBlank(message = "guid 不能为空")
    private String guid;

    @ApiModelProperty(value = "eventId")
    @NotBlank(message = "eventId 不能为空")
    private String eventId;

    @ApiModelProperty(value = "status")
    @NotNull(message = "status 不能为空")
    private String status;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
