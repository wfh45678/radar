package com.pgmmers.radar.service.model;



import com.pgmmers.radar.model.query.DataListQuery;
import com.pgmmers.radar.model.query.DataListRecordQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.model.vo.model.DataListMetaVO;
import com.pgmmers.radar.model.vo.model.DataListRecordVO;
import com.pgmmers.radar.model.vo.model.DataListsVO;

import java.util.List;
import java.util.Map;

public interface DataListsService {

    List<DataListsVO> listDataLists(Long scenarioId, Integer status);

    List<DataListRecordVO> listDataListRecords(Long dataListId);

     DataListsVO get(Long id);

     CommonResult list(Long scenarioId);

     CommonResult query(DataListQuery query);

     CommonResult save(DataListsVO dataList);

     CommonResult delete(Long[] id);

    //
     DataListMetaVO getMeta(Long id);

     CommonResult listMeta(Long dataListId);

     CommonResult saveMeta(List<DataListMetaVO> listDataListMeta);

     CommonResult deleteMeta(List<Long> listId);

    //
     DataListRecordVO getRecord(Long id);

     CommonResult queryRecord(DataListRecordQuery query);

     CommonResult saveRecord(DataListRecordVO dataListRecord);

     CommonResult deleteRecord(Long[] id);
    
     Map<String, Object> getDataListMap(Long scenarioId);

     CommonResult batchImportData(List<DataListsVO> list, Long scenarioId);

     CommonResult batchImportDataRecord(List<DataListRecordVO> list, Long dataListId);
}
