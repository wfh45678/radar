package com.pgmmers.radar.dal.model;

import java.util.List;


import com.pgmmers.radar.dal.bean.DataListQuery;
import com.pgmmers.radar.dal.bean.DataListRecordQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.vo.model.DataListMetaVO;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import com.pgmmers.radar.vo.model.DataListsVO;

public interface DataListDal {

     List<DataListMetaVO> listDataMeta(Long dataListId);

     List<DataListRecordVO> listDataRecord(Long dataListId);

     DataListMetaVO getListMetaById(Long metaId);

     List<DataListsVO> listDataLists(Long modelId, Integer status);

    //
     DataListsVO get(Long id);

     List<DataListsVO> list(Long modelId);

     PageResult<DataListsVO> query(DataListQuery query);

     int save(DataListsVO dataList);

     int delete(Long[] id);

    //
     DataListMetaVO getMeta(Long id);

     int saveMeta(DataListMetaVO dataListMeta);

     int deleteMeta(List<Long> listId);

    //
     DataListRecordVO getRecord(Long id);

     PageResult<DataListRecordVO> queryRecord(DataListRecordQuery query);

     int saveRecord(DataListRecordVO dataListRecord);

     int deleteRecord(Long[] id);

     int deleteRecord(Long dataListId);

}
