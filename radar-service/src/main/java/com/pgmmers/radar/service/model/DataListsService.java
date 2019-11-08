package com.pgmmers.radar.service.model;



import com.pgmmers.radar.dal.bean.DataListQuery;
import com.pgmmers.radar.dal.bean.DataListRecordQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.model.DataListMetaVO;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import com.pgmmers.radar.vo.model.DataListsVO;

import java.util.List;
import java.util.Map;

public interface DataListsService {

    List<DataListsVO> listDataLists(Long modelId, Integer status);

    List<DataListRecordVO> listDataListRecords(Long dataListId);

     DataListsVO get(Long id);

     CommonResult list(Long modelId);

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
    
     Map<String, Object> getDataListMap(Long modelId);

     CommonResult batchImportData(List<DataListsVO> list, Long modelId);

     CommonResult batchImportDataRecord(List<DataListRecordVO> list, Long dataListId);
}
