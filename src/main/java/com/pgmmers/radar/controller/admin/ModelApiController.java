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


import com.pgmmers.radar.model.query.ModelQuery;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.interceptor.ContextHolder;
import com.pgmmers.radar.service.admin.UserService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.impl.model.transfer.ModelPackage;
import com.pgmmers.radar.service.impl.model.transfer.ModelPackageService;
import com.pgmmers.radar.service.impl.model.transfer.TemplateCatalogService;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.model.vo.admin.UserVO;
import com.pgmmers.radar.model.vo.model.ModelVO;
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
@RequestMapping("/services/v1/model")
@Tag(name = "模型API", description = "模型管理接口相关操作")
public class ModelApiController  {

    public static Logger logger = LoggerFactory.getLogger(ModelApiController.class);

    @Autowired
    private ModelService modelService;

    @Autowired
    private ContextHolder contextHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelPackageService modelPackageService;

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
     * 参数与 /import 一致；label/modelName 缺省取模板默认值。
     */
    @PostMapping("/templates/{key}/import")
    public CommonResult importTemplate(@PathVariable("key") String key,
                                       @RequestParam(value = "label", required = false) String label,
                                       @RequestParam(value = "merchantCode", required = false) String merchantCode,
                                       @RequestParam(value = "modelName", required = false) String modelName) {
        CommonResult result = new CommonResult();
        try {
            String code = merchantCode == null || merchantCode.isBlank()
                    ? contextHolder.getContext().getCode() : merchantCode;
            byte[] body = templateCatalogService.bodyByKey(key);
            ModelPackage pkg = modelPackageService.parse(body);
            java.util.List<String> warnings = modelPackageService.validate(pkg);
            Long newId = templateCatalogService.importTemplate(key, code, modelName, label);
            result.setSuccess(true);
            result.setMsg(warnings.isEmpty()
                    ? "模板创建成功，请构建模型后生效"
                    : "模板创建成功（含 " + warnings.size() + " 条告警），请构建模型后生效");
            result.getData().put("id", newId);
            result.getData().put("warnings", warnings);
        } catch (ModelPackageService.ModelPackageException e) {
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
        ModelVO modelVO = modelService.getModelById(id);
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
        result.getData().put("modelList",  modelService.listModel(contextHolder.getContext().getCode(), null));
        return result;
	}

    @GetMapping("/list/{merchantCode}")
	public CommonResult list(@PathVariable String merchantCode) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("modelList",  modelService.listModel(merchantCode, null));
        return result;
	}

    @PostMapping
    public CommonResult query(@RequestBody ModelQuery query, HttpServletRequest request) {
	    CommonResult result = new CommonResult();
        query.setMerchantCode(contextHolder.getContext().getCode());
        return modelService.query(query);
    }


    @PutMapping
    public CommonResult save(@RequestBody ModelVO model, HttpServletRequest request) {
        model.setCode(contextHolder.getContext().getCode());
        return modelService.save(model);
    }


    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return modelService.delete(id);
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
            result = modelService.build(id);
        } catch (Exception e) {
            logger.error("", e);
            result.setMsg("错误:" + e.getMessage());
        }
        return result;
    }


    @PostMapping("/enable/{id}")
    public CommonResult enable(@PathVariable Long id) {
        ModelVO modelVO = modelService.getModelById(id);
        modelVO.setStatus(StatusType.ACTIVE.getKey());
        return modelService.save(modelVO);
    }

    @PostMapping("/disable/{id}")
    public CommonResult disable(@PathVariable Long id) {
        ModelVO modelVO = modelService.getModelById(id);
        modelVO.setStatus(StatusType.INACTIVE.getKey());
        return modelService.save(modelVO);
    }

    @PostMapping("/copy")
	public CommonResult copy(@RequestBody ModelVO model, HttpServletRequest request) {
        return modelService.copy(model.getId(), contextHolder.getContext().getCode(), model.getModelName(), model.getLabel());
	}

    /**
     * 导出模型策略包（v3.2）：模型配置全图 JSON 附件下载。
     */
    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> export(@PathVariable Long id) {
        ModelPackage pkg = modelPackageService.export(id);
        if (pkg == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] body = modelPackageService.serialize(pkg);
        String asciiName = "radar-model-" + id + ".json";
        String encodedName = URLEncoder.encode(modelPackageService.exportFilename(pkg), StandardCharsets.UTF_8)
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
                                    @RequestParam(value = "modelName", required = false) String modelName) {
        CommonResult result = new CommonResult();
        try {
            String code = merchantCode == null || merchantCode.isBlank()
                    ? contextHolder.getContext().getCode() : merchantCode;
            if (label == null || label.isBlank()) {
                result.setMsg("请填写模型显示名称");
                return result;
            }
            ModelPackage pkg = modelPackageService.parse(file.getBytes());
            List<String> warnings = modelPackageService.validate(pkg);
            Long newId = modelPackageService.importPackage(pkg, code, modelName, label.trim());
            result.setSuccess(true);
            result.setMsg(warnings.isEmpty()
                    ? "导入成功，请构建模型后生效"
                    : "导入成功（含 " + warnings.size() + " 条告警），请构建模型后生效");
            result.getData().put("id", newId);
            result.getData().put("warnings", warnings);
        } catch (ModelPackageService.ModelPackageException e) {
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
        result.getData().put("modelList",  modelService.listTemplateModel(true));
        return result;
    }
}
