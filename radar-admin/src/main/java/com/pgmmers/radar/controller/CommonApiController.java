package com.pgmmers.radar.controller;


import com.alibaba.excel.util.IoUtils;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.impl.engine.Plugin.PluginManager;
import com.pgmmers.radar.util.RandomValidateCode;
import com.pgmmers.radar.util.ZipUtils;
import com.pgmmers.radar.vo.common.PluginVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/services/v1/common")
@Api(value = "CommonApi", description = "公用相关操作",  tags = {"公用API"})
public class CommonApiController {

    public static Logger logger = LoggerFactory.getLogger(CommonApiController.class);

    @Value("${sys.conf.workdir}")
    public String workDir;

    @GetMapping("/plugins")
    public CommonResult plugins() {
        CommonResult result = new CommonResult();
        List<PluginVO> plugins=PluginManager.pluginServiceMap()
                .values()
                .stream()
                .map(t-> new PluginVO(t.key(),t.pluginName(),t.desc()))
                .collect(Collectors.toList());
        result.setSuccess(true);
        result.getData().put("plugins", plugins);
        return result;
    }

    @GetMapping("/fieldtypes")
    public CommonResult fieldTypes() {
        CommonResult result = new CommonResult();
        List<HashMap<String,Object>> fields = new ArrayList<>();
        for (FieldType ft : FieldType.values()) {
            //fields.add(ft.name());
        	HashMap<String,Object> map=new LinkedHashMap<>();
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

    @PostMapping(value = "/upload")
    public CommonResult upload(@ApiParam(value = "file") @RequestPart("file") MultipartFile file, @RequestParam(defaultValue = "") String key) {
        CommonResult result = new CommonResult();
        String fileName = file.getOriginalFilename();
        try {
            String path = workDir + "/" + fileName;
            String decomposePath = path.substring(0, path.lastIndexOf("."));
            FileOutputStream fos = new FileOutputStream(new File(workDir + "/" + fileName));
            IoUtils.copy(file.getInputStream(), fos);
            fos.flush();
            fos.close();
            if (!StringUtils.isEmpty(key) && key.equals("machine")) {
                ZipUtils.unZipIt(path, decomposePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return result;
    }

}
