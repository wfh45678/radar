package com.pgmmers.radar.controller;


import com.pgmmers.radar.dal.bean.DataListRecordQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services/v1/datalistrecord")
public class DataListRecordApiController {

    @Autowired
    private DataListsService dataListsService;

    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        DataListRecordVO dataListRecord = dataListsService.getRecord(id);
        if (dataListRecord != null) {
            result.setSuccess(true);
            result.getData().put("record", dataListRecord);
        }
        return result;
    }

    @PostMapping
    public CommonResult query(@RequestBody DataListRecordQuery query) {
        return dataListsService.queryRecord(query);
    }


    @PutMapping
    public CommonResult save(@RequestBody DataListRecordVO dataListRecord) {
        return dataListsService.saveRecord(dataListRecord);
    }


    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return dataListsService.deleteRecord(id);
    }

}
