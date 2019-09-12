package com.pgmmers.radar.controller;

import com.pgmmers.radar.dal.bean.FieldQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.vo.model.FieldVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services/v1/field")
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
