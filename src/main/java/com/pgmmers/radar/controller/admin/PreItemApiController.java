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


import com.pgmmers.radar.model.query.PreItemQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.PreItemService;
import com.pgmmers.radar.model.vo.model.PreItemVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services/v1/preitem")
@Tag(name = "预处理API", description = "预处理接口相关操作")
public class PreItemApiController {

    @Autowired
    private PreItemService preItemService;

    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        PreItemVO preItemVO = preItemService.get(id);
        if (preItemVO != null) {
            result.setSuccess(true);
            result.getData().put("preItem", preItemVO);
        }
        return result;
    }


    @GetMapping("/list/{modelId}")
    public CommonResult list(@PathVariable Long modelId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", preItemService.listPreItem(modelId));
        return result;
    }

    @PostMapping
    public CommonResult query(@RequestBody PreItemQuery query) {
        return preItemService.query(query);
    }

    @PutMapping
    public CommonResult save(@RequestBody PreItemVO preItem) {
        return preItemService.save(preItem);
    }

    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return preItemService.delete(id);
    }

}
