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


import com.alibaba.excel.util.IoUtils;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.util.CaptchaUtil;
import com.pgmmers.radar.util.ZipUtils;
import com.pgmmers.radar.vo.common.PluginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services/v1/common")
@Api(value = "CommonApi", description = "公用相关操作",  tags = {"公用API"})
public class CommonApiController {

    public static Logger logger = LoggerFactory.getLogger(CommonApiController.class);

    @Value("${sys.conf.workdir}")
    public String workDir;

    @Autowired
    private CacheService cacheService;
    @Autowired
    private PluginManager pluginManager;


    @GetMapping("/plugins")
    public CommonResult plugins() {
        CommonResult result = new CommonResult();
        List<PluginVO> plugins = pluginManager.getPluginServiceMap()
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


    @GetMapping(value = "/getCaptcha", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity getCaptcha() {
        CaptchaUtil captchaUtil = new CaptchaUtil();
        CaptchaUtil.Captcha captcha = captchaUtil.genRandcode();
        cacheService.cacheCaptcha(captcha.getCaptcha().toUpperCase());
        return ResponseEntity.ok(captcha.getContents());
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
