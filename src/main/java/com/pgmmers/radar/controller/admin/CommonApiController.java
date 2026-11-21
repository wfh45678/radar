/*
 * Copyright (c) 2019 WangFeiHu
 *  Radar is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *  http://license.coscl.org.cn/MulanPSL2
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 */

package com.pgmmers.radar.controller.admin;


import com.alibaba.excel.util.IoUtils;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.util.CaptchaUtil;
import com.pgmmers.radar.common.util.ZipUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services/v1/common")
@Tag(name = "公用API", description = "公用相关操作")
public class CommonApiController {

    public static Logger logger = LoggerFactory.getLogger(CommonApiController.class);

    @Value("${sys.conf.workdir}")
    public String workDir;

    @Autowired
    private CacheService cacheService;
    @Autowired
    private PluginManager pluginManager;


    /** 插件清单（v3.1）：仅启用条目，含参数 schema 与输出列元信息，管理端据此动态渲染配置表单。 */
    @GetMapping("/plugins")
    public CommonResult plugins() {
        CommonResult result = new CommonResult();
        List<Map<String, Object>> plugins = pluginManager.activeEntries()
                .stream()
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("key", e.getKey());
                    item.put("method", e.getKey());
                    item.put("pluginName", e.getName());
                    item.put("desc", e.getDesc());
                    item.put("type", e.getType());
                    item.put("remote", e.isRemote());
                    item.put("source", e.getSource().name());
                    item.put("version", e.getVersion());
                    item.put("provider", e.getProvider());
                    item.put("paramSchema", e.getParamSchema());
                    item.put("outputMeta", e.getOutputMeta());
                    return item;
                })
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
    public CommonResult upload(@Parameter(description = "file") @RequestPart("file") MultipartFile file, @RequestParam(defaultValue = "") String key) {
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
