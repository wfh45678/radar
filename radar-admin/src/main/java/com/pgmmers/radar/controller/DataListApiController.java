package com.pgmmers.radar.controller;


import com.pgmmers.radar.dal.bean.DataListQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.vo.model.DataListsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services/v1/datalist")
public class DataListApiController {

    @Autowired
    private DataListsService dataListsService;

    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        DataListsVO dataList = dataListsService.get(id);
        if (dataList != null) {
            result.setSuccess(true);
            result.getData().put("datalist", dataList);
        }
        return result;
    }

    @GetMapping("/list/{modelId}")
    public CommonResult list(@PathVariable Long modelId) {
        return dataListsService.list(modelId);
    }

    @PostMapping
    public CommonResult query(@RequestBody DataListQuery query) {
        return dataListsService.query(query);
    }


    @PutMapping
    public CommonResult save(@RequestBody DataListsVO dataList) {
        return dataListsService.save(dataList);
    }

    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return dataListsService.delete(id);
    }

}
