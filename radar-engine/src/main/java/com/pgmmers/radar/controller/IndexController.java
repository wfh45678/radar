package com.pgmmers.radar.controller;

import com.pgmmers.radar.service.common.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * engine 启动首页.
 * @author wangfeihu
 */
@RestController
public class IndexController {

    @GetMapping(value = {"/", ""})
    public CommonResult index(HttpServletRequest request) {
        CommonResult result =  new CommonResult(Boolean.TRUE, "100", "Engine is running");
        result.getData().put("swagger url:", request.getRequestURL() + "swagger-ui.html");
        return result;
    }
}
