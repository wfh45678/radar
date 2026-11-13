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
import com.pgmmers.radar.service.model.ModelConfParamService;
import com.pgmmers.radar.service.model.ModelConfService;
import com.pgmmers.radar.model.vo.model.ModelConfParamVO;
import com.pgmmers.radar.model.vo.model.ModelConfVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 机器学习配置 api.
 * @author wangfeihu
 */
@RestController
@RequestMapping("/services/v1/modelConfigParam")
@Tag(name = "机器学习配置API", description = "模型机器学习配置相关操作")
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
