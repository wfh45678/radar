package com.pgmmers.radar.service.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@ApiModel(description="通用Result")
public class CommonResult implements Serializable{

    static final long serialVersionUID = 234345123998231L;


    @ApiModelProperty(value="成功标识",name="success")
    protected boolean success = Boolean.FALSE;

    @ApiModelProperty(value="结果描叙",name="msg")
    protected String msg = "";

    @ApiModelProperty(value="结果代码",name="code")
    protected String code = "100";

    @ApiModelProperty(value="其它数据",name="data")
    protected Map<String, Object> data = new HashMap<>();

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }


    public CommonResult () {

    }

    public CommonResult(boolean success, String code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "CommonResult [success=" + success + ", msg=" + msg + ", code=" + code + "]";
    }

}
