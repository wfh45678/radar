package com.pgmmers.radar.controller;

import com.pgmmers.radar.dal.bean.DataListQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.util.ExcelImportUtil;
import com.pgmmers.radar.util.ExportExcelInfo;
import com.pgmmers.radar.vo.model.DataListsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/services/v1/datalist")
@Api(value = "DataListsApi", description = "黑白名单列表接口相关操作",  tags = {"数据列表API"})
public class DataListApiController {

    public static Logger logger = LoggerFactory.getLogger(DataListApiController.class);

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

    /**
     *
     * 批量导入黑/白名单管理
     * @param file    文件
     * @param modelId 模型ID
     * @return
     * @author xushuai
     */
    @PostMapping(value = "/batchImportData")
    public CommonResult batchImportData(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile file, @RequestParam(value = "modelId", required = true)Long modelId) {
        CommonResult result = new CommonResult();
        result.setSuccess(false);
        String fileName = file.getOriginalFilename();
        if (fileName != null && !(fileName.contains(".xls") || fileName.contains(".xlsx"))) {
            result.setMsg("传入的件格式有误!");
            return result;
        }
        ExportExcelInfo<DataListsVO> info = getImportMeta();
        List<Map<String, Object>> listError = new ArrayList<>();
        List<DataListsVO> list = null;
        try {
            list = ExcelImportUtil.excelToList(file.getInputStream(), info, listError, DataListsVO.class);
        } catch (Exception e) {
            logger.error("导入Excel失败:" + e.getMessage());
        }
        if (list == null || list.size() == 0) {
            result.setMsg("无导入数据!");
            return result;
        }
        if (list.size() > 1000) {
            result.setMsg("最大导入不能超过" + 1000 + "条");
            return result;
        }
        return dataListsService.batchImportData(list, modelId);
    }

    private ExportExcelInfo<DataListsVO> getImportMeta() {
        ExportExcelInfo<DataListsVO> info = new ExportExcelInfo<DataListsVO>(null);
        info.addExcelColumn("备注", "comment");
        info.addExcelColumn("列表名", "label");
        info.addExcelColumn("名单类型", "listType");
        return info;
    }
}
