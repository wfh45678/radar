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


import com.pgmmers.radar.dal.bean.DataListRecordQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.util.ExcelImportUtil;
import com.pgmmers.radar.util.ExportExcelInfo;
import com.pgmmers.radar.vo.model.DataListRecordVO;
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
@RequestMapping("/services/v1/datalistrecord")
@Api(value = "DataListRecordApi", description = "列表内容维护接口相关操作",  tags = {"列表内容API"})
public class DataListRecordApiController {

    public static Logger logger = LoggerFactory.getLogger(DataListRecordApiController.class);

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

    /**
     *
     * 批量导入黑/白名单管理
     * @param file    文件
     * @param dataListId  数据列表ID
     * @return
     * @author xushuai
     */
    @PostMapping(value = "/batchImportDataRecord")
    public CommonResult batchImportDataRecord(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile file, @RequestParam(value = "dataListId", required = true)Long dataListId) {
        CommonResult result = new CommonResult();
        result.setSuccess(false);
        String fileName = file.getOriginalFilename();
        if (fileName != null && !(fileName.contains(".xls") || fileName.contains(".xlsx"))) {
            result.setMsg("传入的件格式有误!");
            return result;
        }
        ExportExcelInfo<DataListRecordVO> info = getImportMeta();
        List<Map<String, Object>> listError = new ArrayList<>();
        List<DataListRecordVO> list = null;
        try {
            list = ExcelImportUtil.excelToList(file.getInputStream(), info, listError, DataListRecordVO.class);
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
        return dataListsService.batchImportDataRecord(list, dataListId);
    }

    private ExportExcelInfo<DataListRecordVO> getImportMeta() {
        ExportExcelInfo<DataListRecordVO> info = new ExportExcelInfo<DataListRecordVO>(null);
        info.addExcelColumn("dataRecord", "dataRecord");
        info.addExcelColumn("dataRemark", "dataRemark");
        return info;
    }

}
