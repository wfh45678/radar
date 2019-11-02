package com.pgmmers.radar.controller;


import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.enums.PluginType;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.util.RandomValidateCode;
import com.pgmmers.radar.vo.common.PluginVO;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/services/v1/common")
@Api(value = "CommonApi", description = "公用相关操作",  tags = {"公用API"})
public class CommonApiController {

    public static Logger logger = LoggerFactory.getLogger(CommonApiController.class);

    
    @GetMapping("/plugins")
    public CommonResult plugins() {
        CommonResult result = new CommonResult();
        List<PluginVO> plugins = new ArrayList<PluginVO>();
        for (PluginType pt : PluginType.values()) {
            plugins.add(new PluginVO(pt));
        }
        result.setSuccess(true);
        result.getData().put("plugins", plugins);
        return result;
    }

    @GetMapping("/fieldtypes")
    public CommonResult fieldTypes() {
        CommonResult result = new CommonResult();
        List<HashMap<String,Object>> fields = new ArrayList<HashMap<String,Object>>();
        for (FieldType ft : FieldType.values()) {
            //fields.add(ft.name());
        	HashMap<String,Object> map=new LinkedHashMap<String,Object>();
        	map.put("name", ft.name());
        	map.put("desc", ft.getDesc());
        	fields.add(map);
        }
        result.setSuccess(true);
        result.getData().put("fields", fields);
        return result;
    }


    @GetMapping("/getCaptcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("image/jpeg");// 设置相应类型,告诉浏览器输出的内容为图片
        response.setHeader("Pragma", "No-cache");// 设置响应头信息，告诉浏览器不要缓存此内容
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expire", 0);
        RandomValidateCode randomValidateCode = new RandomValidateCode();
        try {
            randomValidateCode.genRandcode(request, response);//输出图片方法
        } catch (Exception e) {
            logger.error("get captcha error", e);
        }
    }

    /**
     * 获取代码贡献历史。
     * @return
     */
    @GetMapping("/getContributorInfo")
    public CommonResult getContributorInfo() {
        CommonResult result = new CommonResult();
        //TODO:
        return result;
    }

}
