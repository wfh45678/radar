package com.pgmmers.radar.controller;

import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.ModelConfService;
import com.pgmmers.radar.vo.model.ModelConfVO;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 机器学习配置 api.
 * @author wangfeihu
 */
@RestController
@RequestMapping("/services/v1/modelConfig")
@Api(value = "ModelConfigApi", description = "模型机器学习配置相关操作",  tags = {"机器学习配置API"})
public class ModelConfigApiController {

    @Autowired
    private ModelConfService modelConfService;

    @GetMapping("/list/{modelId}")
    public CommonResult list(@PathVariable Long modelId) {
        CommonResult result = new CommonResult();
        ModelConfVO modelConfVO = modelConfService.getByModelId(modelId);
        if (modelConfVO != null) {
            result.getData().put("modelConfig", modelConfVO);
        }
        result.setSuccess(true);
        return result;
    }

    @PutMapping
    public CommonResult save(@RequestBody ModelConfVO modelConf) {
        CommonResult result = new CommonResult();

        if (modelConf.getId() == -1L) {
           modelConf.setId(null);
        }
        if (!validate(modelConf)) {
            result.setMsg("请按照提示输入必要字段！");
            return result;
        }
        modelConfService.save(modelConf);
        result.setSuccess(true);
        return result;
    }

    /**
     * 检验上传字段。
     * @param modelConf
     * @return
     * @author wangfeihu
     */
    private boolean validate(ModelConfVO modelConf) {
        boolean result = true;
        if (StringUtils.isEmpty(modelConf.getName())
                || StringUtils.isEmpty(modelConf.getOperation())
                || StringUtils.isEmpty(modelConf.getTag())
                || StringUtils.isEmpty(modelConf.getPath())) {
            result = false;
        }
        if (modelConf.getId() == null) {
            if ( StringUtils.isEmpty(modelConf.getConfParam().getFeed())
                    || StringUtils.isEmpty(modelConf.getConfParam().getExpressions())) {
                result = false;
            }
        }
        return result;
    }

}
