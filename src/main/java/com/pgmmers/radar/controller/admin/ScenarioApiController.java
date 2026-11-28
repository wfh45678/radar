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


import com.pgmmers.radar.model.query.ScenarioQuery;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.interceptor.ContextHolder;
import com.pgmmers.radar.service.admin.UserService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.impl.model.transfer.ScenarioPackage;
import com.pgmmers.radar.service.impl.model.transfer.ScenarioPackageService;
import com.pgmmers.radar.service.impl.model.transfer.TemplateCatalogService;
import com.pgmmers.radar.service.model.ScenarioService;
import com.pgmmers.radar.model.vo.admin.UserVO;
import com.pgmmers.radar.model.vo.model.ScenarioVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/services/v1/scenario")
@Tag(name = "模型API", description = "模型管理接口相关操作")
public class ScenarioApiController  {

    public static Logger logger = LoggerFactory.getLogger(ScenarioApiController.class);

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private ContextHolder contextHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private ScenarioPackageService scenarioPackageService;

    @Autowired
    private TemplateCatalogService templateCatalogService;

    /**
     * 官方模板库（v3.4）：内置 + 在线源合并的模板卡片列表（含来源徽标与在线源状态）。
     */
    @GetMapping("/templates")
    public CommonResult templates() {
        CommonResult result = new CommonResult();
        try {
            java.util.Map<String, Object> library = templateCatalogService.library();
            result.setSuccess(true);
            result.getData().putAll(library);
        } catch (Exception e) {
            logger.error("template library load failed", e);
            result.setMsg("模板库加载失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 从模板一键创建（v3.4）：按 key 加载（classpath/在线源）→ validate → 导入为新模型。
     * 参数与 /import 一致；label/scenarioName 缺省取模板默认值。
     */
    @PostMapping("/templates/{key}/import")
    public CommonResult importTemplate(@PathVariable("key") String key,
                                       @RequestParam(value = "label", required = false) String label,
                                       @RequestParam(value = "merchantCode", required = false) String merchantCode,
                                       @RequestParam(value = "scenarioName", required = false) String scenarioName) {
        CommonResult result = new CommonResult();
        try {
            String code = merchantCode == null || merchantCode.isBlank()
                    ? contextHolder.getContext().getCode() : merchantCode;
            byte[] body = templateCatalogService.bodyByKey(key);
            ScenarioPackage pkg = scenarioPackageService.parse(body);
            java.util.List<String> warnings = scenarioPackageService.validate(pkg);
            Long newId = templateCatalogService.importTemplate(key, code, scenarioName, label);
            result.setSuccess(true);
            result.setMsg(warnings.isEmpty()
                    ? "模板创建成功，请构建模型后生效"
                    : "模板创建成功（含 " + warnings.size() + " 条告警），请构建模型后生效");
            result.getData().put("id", newId);
            result.getData().put("warnings", warnings);
        } catch (ScenarioPackageService.ScenarioPackageException e) {
            logger.warn("template import rejected: {}", e.getMessage());
            result.setMsg(e.getMessage());
        } catch (Exception e) {
            logger.error("template import failed: {}", key, e);
            result.setMsg("模板创建失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        ScenarioVO modelVO = scenarioService.getScenarioById(id);
        if (modelVO != null) {
            result.setSuccess(true);
            result.getData().put("model", modelVO);
        }
        return result;
    }

    @GetMapping("/list")
	public CommonResult list(HttpServletRequest request) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("scenarioList",  scenarioService.listScenario(contextHolder.getContext().getCode(), null));
        return result;
	}

    @GetMapping("/list/{merchantCode}")
	public CommonResult list(@PathVariable String merchantCode) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("scenarioList",  scenarioService.listScenario(merchantCode, null));
        return result;
	}

    @PostMapping
    public CommonResult query(@RequestBody ScenarioQuery query, HttpServletRequest request) {
	    CommonResult result = new CommonResult();
        query.setMerchantCode(contextHolder.getContext().getCode());
        return scenarioService.query(query);
    }


    @PutMapping
    public CommonResult save(@RequestBody ScenarioVO model, HttpServletRequest request) {
        model.setCode(contextHolder.getContext().getCode());
        return scenarioService.save(model);
    }


    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return scenarioService.delete(id);
    }

    @PostMapping("/build/{id}")
    public CommonResult build(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        String userName = contextHolder.getContext().getUsername();
        UserVO userVO = userService.getByUserName(userName);
        if (userVO.getVipLevel() <= 0) {
            result.setSuccess(false);
            result.setMsg("服务器资源有限，请联系作者开通VIP权限！");
            return result;
        }
        try {
            result = scenarioService.build(id);
        } catch (Exception e) {
            logger.error("", e);
            result.setMsg("错误:" + e.getMessage());
        }
        return result;
    }


    @PostMapping("/enable/{id}")
    public CommonResult enable(@PathVariable Long id) {
        ScenarioVO modelVO = scenarioService.getScenarioById(id);
        modelVO.setStatus(StatusType.ACTIVE.getKey());
        return scenarioService.save(modelVO);
    }

    @PostMapping("/disable/{id}")
    public CommonResult disable(@PathVariable Long id) {
        ScenarioVO modelVO = scenarioService.getScenarioById(id);
        modelVO.setStatus(StatusType.INACTIVE.getKey());
        return scenarioService.save(modelVO);
    }

    @PostMapping("/copy")
	public CommonResult copy(@RequestBody ScenarioVO model, HttpServletRequest request) {
        return scenarioService.copy(model.getId(), contextHolder.getContext().getCode(), model.getScenarioName(), model.getLabel());
	}

    /**
     * 导出模型策略包（v3.2）：模型配置全图 JSON 附件下载。
     */
    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> export(@PathVariable Long id) {
        ScenarioPackage pkg = scenarioPackageService.export(id);
        if (pkg == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] body = scenarioPackageService.serialize(pkg);
        String asciiName = "radar-scenario-" + id + ".json";
        String encodedName = URLEncoder.encode(scenarioPackageService.exportFilename(pkg), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /**
     * 导入模型策略包（v3.2）：上传 JSON → 校验报告 → 导入为新模型（INIT，构建后生效）。
     */
    @PostMapping("/import")
    public CommonResult importModel(@Parameter(description = "策略包 json 文件") @RequestPart("file") MultipartFile file,
                                    @RequestParam("label") String label,
                                    @RequestParam(value = "merchantCode", required = false) String merchantCode,
                                    @RequestParam(value = "scenarioName", required = false) String scenarioName) {
        CommonResult result = new CommonResult();
        try {
            String code = merchantCode == null || merchantCode.isBlank()
                    ? contextHolder.getContext().getCode() : merchantCode;
            if (label == null || label.isBlank()) {
                result.setMsg("请填写模型显示名称");
                return result;
            }
            ScenarioPackage pkg = scenarioPackageService.parse(file.getBytes());
            List<String> warnings = scenarioPackageService.validate(pkg);
            Long newId = scenarioPackageService.importPackage(pkg, code, scenarioName, label.trim());
            result.setSuccess(true);
            result.setMsg(warnings.isEmpty()
                    ? "导入成功，请构建模型后生效"
                    : "导入成功（含 " + warnings.size() + " 条告警），请构建模型后生效");
            result.getData().put("id", newId);
            result.getData().put("warnings", warnings);
        } catch (ScenarioPackageService.ScenarioPackageException e) {
            logger.warn("model package import rejected: {}", e.getMessage());
            result.setMsg(e.getMessage());
        } catch (Exception e) {
            logger.error("model package import failed", e);
            result.setMsg("导入失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/list/template")
    public CommonResult template() {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("scenarioList",  scenarioService.listTemplateScenario(true));
        return result;
    }
}
