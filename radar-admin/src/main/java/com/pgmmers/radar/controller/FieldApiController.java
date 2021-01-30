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

import com.pgmmers.radar.dal.bean.FieldQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.vo.model.FieldVO;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services/v1/field")
@Api(value = "FieldApi", description = "字段管理接口相关操作",  tags = {"字段管理API"})
public class FieldApiController {

    @Autowired
    private FieldService fieldService;


    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        FieldVO fieldVO = fieldService.get(id);
        if (fieldVO != null) {
            result.setSuccess(true);
            result.getData().put("field", fieldVO);
        }
        return result;
    }

    @PostMapping
    public CommonResult query(@RequestBody FieldQuery query) {
        return fieldService.query(query);
    }

    @GetMapping("/list/{modelId}")
    public CommonResult list(@PathVariable Long modelId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("field", fieldService.listField(modelId));
        return result;
    }


    @PutMapping
    public CommonResult save(@RequestBody FieldVO field) {
        return fieldService.save(field);
    }

    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return fieldService.delete(id);
    }

}
