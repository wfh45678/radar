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

import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.ModelConfParamService;
import com.pgmmers.radar.service.model.ModelConfService;
import com.pgmmers.radar.vo.model.ModelConfParamVO;
import com.pgmmers.radar.vo.model.ModelConfVO;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 机器学习配置 api.
 * @author wangfeihu
 */
@RestController
@RequestMapping("/services/v1/modelConfigParam")
@Api(value = "ModelConfigApi", description = "模型机器学习配置相关操作",  tags = {"机器学习配置API"})
public class ModelConfigParamApiController {

    @Autowired
    private ModelConfParamService modelParamService;

    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        ModelConfParamVO paramVO = modelParamService.get(id);
        result.getData().put("param", paramVO);
        result.setSuccess(true);
        return result;
    }

    @PutMapping
    public CommonResult save(@RequestBody ModelConfParamVO modelConf) {
        CommonResult result = new CommonResult();
        if (modelConf.getId() == -1L) {
           modelConf.setId(null);
        }
        modelParamService.save(modelConf);
        result.setSuccess(true);
        return result;
    }



}
