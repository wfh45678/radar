/*
 * Copyright (c) 2019-2022 WangFeiHu
 *  Radar is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *  http://license.coscl.org.cn/MulanPSL2
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 */

package com.pgmmers.radar.controller.engine;


import com.pgmmers.radar.service.RiskAnalysisEngineService;
import com.pgmmers.radar.service.common.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/services/v1")
@Tag(name = "风险分析API(引擎端)", description = "接受用户事件数据，实时进行分析并返回分析结果。")
public class MainController {

    @Autowired
    private RiskAnalysisEngineService engineApi;


    @Deprecated
    @PostMapping("/uploadInfo")
    @Operation(summary = "事件数据提交接口")
    public CommonResult upload(@RequestParam @Parameter(name="modelGuid", description ="模型Guid", required=true) String modelGuid,
                               @RequestParam @Parameter(name="reqId",description ="请求流水号",required=true) String reqId,
                               @RequestParam @Parameter(name="jsonInfo",description ="事件内容(json 格式)",required=true) String jsonInfo) {
        CommonResult result = engineApi.uploadInfo(modelGuid, reqId, jsonInfo);
        return result;
    }

    @GetMapping("/getScore")
    @Operation(summary = "查询事件处理结果")
    public CommonResult getScore(@RequestParam  @Parameter(name="modelGuid",description ="模型Guid",required=true)  String modelGuid,
                           @RequestParam  @Parameter(name="reqId",description ="请求流水号",required=true)  String reqId) {
        CommonResult result = engineApi.getScore(modelGuid, reqId);
        return result;
    }

    @PostMapping("/upload")
    @Operation(summary = "事件数据提交接口")
    public CommonResult upload(@Valid @RequestBody EventRequest request) {
        CommonResult result = engineApi.uploadInfo(request.getGuid(), request.getReqId(), request.getJsonInfo());
        return result;
    }

    @PostMapping("/syncStatus")
    @Operation(summary = "事件状态同步接口")
    public CommonResult syncStatus(@Valid @RequestBody StatusSyncRequest request) {
        CommonResult result = engineApi.syncStatus(request.getGuid(), request.getEventId(), request.getStatus());
        return result;
    }
}
