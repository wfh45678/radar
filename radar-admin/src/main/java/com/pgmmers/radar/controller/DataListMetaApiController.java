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
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.vo.model.DataListMetaVO;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/services/v1/datalistmeta")
@Api(value = "DataListMetaApi", description = "列表字段接口相关操作",  tags = {"列表字段API"})
public class DataListMetaApiController {

    @Autowired
    private DataListsService dataListsService;

    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        DataListMetaVO dataListMeta = dataListsService.getMeta(id);
        if (dataListMeta != null) {
            result.setSuccess(true);
            result.getData().put("meta", dataListMeta);
        }
        return result;
    }

    @GetMapping("/list/{dataListId}")
    public CommonResult list(@PathVariable Long dataListId) {
        return dataListsService.listMeta(dataListId);
    }


    @PutMapping
    public CommonResult save(@RequestBody List<DataListMetaVO> listDataListMeta) {
        return dataListsService.saveMeta(listDataListMeta);
    }


    @DeleteMapping
    public CommonResult delete(List<Long> listId) {
        return dataListsService.deleteMeta(listId);
    }

}
