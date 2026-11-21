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

import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.impl.engine.plugin.PluginAdminService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 插件生命周期管理 API（v3.1）：上传 / 列表 / 启停 / 删除 / 手动重载。
 * 路径在 /services/v1/** 鉴权范围内（JWT），仅管理端使用。
 *
 * @since 3.1.0
 */
@RestController
@RequestMapping("/services/v1/plugins")
@Tag(name = "插件管理", description = "插件上传、启停、删除与执行统计（v3.1）")
public class PluginApiController {

    public static Logger logger = LoggerFactory.getLogger(PluginApiController.class);

    @Autowired
    private PluginAdminService pluginAdminService;

    /** 全量插件列表（含版本/来源/启停/执行统计快照与引擎插件配置）。 */
    @GetMapping
    public CommonResult list() {
        CommonResult result = new CommonResult();
        Map<String, Object> data = pluginAdminService.list();
        result.setSuccess(true);
        result.getData().putAll(data);
        return result;
    }

    /** 上传外置插件 jar（热重载开启时即时生效，否则重启生效）。 */
    @PostMapping
    public CommonResult upload(@Parameter(description = "插件 jar 文件") @RequestPart("file") MultipartFile file) {
        CommonResult result = new CommonResult();
        try {
            Map<String, Object> upload = pluginAdminService.upload(file);
            result.setSuccess(true);
            result.setMsg("上传成功" + (Boolean.TRUE.equals(upload.get("restartRequired"))
                    ? "（当前配置为重启生效，请重启引擎）" : "，已热重载生效"));
            result.getData().put("upload", upload);
        } catch (Exception e) {
            logger.warn("plugin upload rejected: {}", e.getMessage());
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /** 启用/禁用插件（即时生效，重启后保持）。 */
    @PutMapping("/{key}/enabled")
    public CommonResult setEnabled(@PathVariable("key") String key,
                                   @RequestParam("enabled") boolean enabled) {
        CommonResult result = new CommonResult();
        try {
            pluginAdminService.setEnabled(key, enabled);
            result.setSuccess(true);
            result.setMsg(enabled ? "已启用" : "已禁用");
        } catch (Exception e) {
            logger.warn("plugin enable toggle failed: {}, {}", key, e.getMessage());
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /** 删除外置插件（删 jar + 重载；Windows 宽限期内文件占用会明确报错）。 */
    @DeleteMapping("/{key}")
    public CommonResult delete(@PathVariable("key") String key) {
        CommonResult result = new CommonResult();
        try {
            Map<String, Object> deleted = pluginAdminService.delete(key);
            result.setSuccess(true);
            result.setMsg(Boolean.TRUE.equals(deleted.get("restartRequired"))
                    ? "已删除，重启引擎后完全卸载" : "已删除并重载生效");
            result.getData().put("deleted", deleted);
        } catch (Exception e) {
            logger.warn("plugin delete failed: {}, {}", key, e.getMessage());
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /** 手动整代重载外置插件（plugins/ 目录手工换 jar 后的人工兜底）。 */
    @PostMapping("/reload")
    public CommonResult reload() {
        CommonResult result = new CommonResult();
        try {
            result.setSuccess(true);
            result.setMsg("重载完成");
            result.getData().put("registered", pluginAdminService.reload());
        } catch (Exception e) {
            logger.warn("plugin reload failed", e);
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /** 校验失败统一转 CommonResult（msg 可直接展示）。 */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public CommonResult onValidation(Exception e) {
        CommonResult result = new CommonResult();
        result.setMsg(e.getMessage());
        return result;
    }
}
