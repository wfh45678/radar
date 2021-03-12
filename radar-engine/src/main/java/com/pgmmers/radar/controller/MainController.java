/*
 * Copyright (c) 2019 WangFeiHu
 *  Radar is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *  http://license.coscl.org.cn/MulanPSL2
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 */

package com.pgmmers.radar.controller;


import com.pgmmers.radar.service.RiskAnalysisEngineService;
import com.pgmmers.radar.service.common.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/services/v1")
@Api(value = "RiskApi", description = "接受用户事件数据，实时进行分析并返回分析结果。",  tags = {"风险分析API(引擎端)"})
public class MainController {

    @Autowired
    private RiskAnalysisEngineService engineApi;


    @Deprecated
    @PostMapping("/uploadInfo")
    @ApiOperation(value = "事件数据提交接口")
    public CommonResult upload(@RequestParam @ApiParam(name="modelGuid", value="模型Guid", required=true) String modelGuid,
                               @RequestParam @ApiParam(name="reqId",value="请求流水号",required=true) String reqId,
                               @RequestParam @ApiParam(name="jsonInfo",value="事件内容(json 格式)",required=true) String jsonInfo) {
        CommonResult result = engineApi.uploadInfo(modelGuid, reqId, jsonInfo);
        return result;
    }

    @GetMapping("/getScore")
    @ApiOperation(value = "查询事件处理结果")
    public CommonResult getScore(@RequestParam  @ApiParam(name="modelGuid",value="模型Guid",required=true)  String modelGuid,
                           @RequestParam  @ApiParam(name="reqId",value="请求流水号",required=true)  String reqId) {
        CommonResult result = engineApi.getScore(modelGuid, reqId);
        return result;
    }

    @PostMapping("/upload")
    @ApiOperation(value = "事件数据提交接口")
    public CommonResult upload(@Valid @RequestBody EventRequest request) {
        CommonResult result = engineApi.uploadInfo(request.getGuid(), request.getReqId(), request.getJsonInfo());
        return result;
    }
}
