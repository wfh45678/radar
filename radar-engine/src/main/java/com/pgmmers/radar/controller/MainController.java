package com.pgmmers.radar.controller;


import com.pgmmers.radar.service.RiskAnalysisEngineService;
import com.pgmmers.radar.service.common.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services/v1")
@Api(value = "RiskApi", description = "接受用户事件数据，实时进行分析并返回分析结果。",  tags = {"风险分析API(引擎端)"})
public class MainController {

    @Autowired
    private RiskAnalysisEngineService engineApi;


    @PostMapping("/uploadInfo")
    @ApiOperation(value = "事件数据提交接口")
    public CommonResult upload(@RequestParam @ApiParam(name="modelGuid", value="模型Guid", required=true) String modelGuid,
                               @RequestParam @ApiParam(name="reqId",value="请求流水号",required=true) String reqId,
                               @RequestParam @ApiParam(name="jsonInfo",value="事件内容(json 格式)",required=true) String jsonInfo) {
        CommonResult result = engineApi.uploadInfo(modelGuid, reqId, jsonInfo);
		result.setSuccess(true);
        return result;
    }

    @GetMapping("/getScore")
    @ApiOperation(value = "查询事件处理结果")
    public CommonResult getScore(@RequestParam  @ApiParam(name="modelGuid",value="模型Guid",required=true)  String modelGuid,
                           @RequestParam  @ApiParam(name="reqId",value="请求流水号",required=true)  String reqId) {
        CommonResult result = engineApi.getScore(modelGuid, reqId);
		result.setSuccess(true);
        return result;
    }
}
