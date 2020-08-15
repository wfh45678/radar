package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson.JSON;
import com.pgmmers.radar.dal.bean.DataListQuery;
import com.pgmmers.radar.dal.bean.DataListRecordQuery;
import com.pgmmers.radar.dal.model.DataListDal;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.vo.model.DataListMetaVO;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import com.pgmmers.radar.vo.model.DataListsVO;
import com.pgmmers.radar.vo.model.ModelVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataListsServiceImpl implements DataListsService, SubscribeHandle {

    public static Logger logger = LoggerFactory.getLogger(DataListsServiceImpl.class);

    @Autowired
    private DataListDal dataListDal;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private ModelService modelService;

    private static Map<Long, Map<String, Object>> dataListRecordCacheMap = new HashMap<>();

    @Override
    public List<DataListsVO> listDataLists(Long modelId, Integer status) {
        return dataListDal.listDataLists(modelId, status);
    }

    @Override
    public List<DataListRecordVO> listDataListRecords(Long dataListId) {
        return dataListDal.listDataRecord(dataListId);
    }

    @Override
    public DataListsVO get(Long id) {
        return dataListDal.get(id);
    }

    @Override
    public CommonResult list(Long modelId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", dataListDal.list(modelId));
        return result;
    }

    @Override
    public CommonResult query(DataListQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", dataListDal.query(query));
        return result;
    }

    @Override
    public CommonResult save(DataListsVO dataList) {
        CommonResult result = new CommonResult();
        int count = dataListDal.save(dataList);
        if (count > 0) {
        	if(StringUtils.isEmpty(dataList.getName())){
        		dataList.setName("dataList_"+dataList.getId());
        		dataListDal.save(dataList);
        	}

            result.getData().put("id", dataList.getId());
            result.setSuccess(true);
            // 通知更新
            dataList.setOpt("new");
            cacheService.publishDataList(dataList);
        }
        return result;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        DataListsVO dataList = dataListDal.get(id[0]);
        int count = dataListDal.delete(id);
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            dataList.setOpt("delete");
            cacheService.publishDataList(dataList);
        }
        return result;
    }

    @Override
    public DataListMetaVO getMeta(Long id) {
        return dataListDal.getMeta(id);
    }

    @Override
    public CommonResult listMeta(Long dataListId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", dataListDal.listDataMeta(dataListId));
        return result;
    }

    @Override
    public CommonResult saveMeta(List<DataListMetaVO> listDataListMeta) {
        // 现有数据转为map
        Map<Long, DataListMetaVO> metaMap = new HashMap<Long, DataListMetaVO>();
        Long dataListId = listDataListMeta.get(0).getDataListId();
        List<DataListMetaVO> listMetaVO = dataListDal.listDataMeta(dataListId);
        for (DataListMetaVO metaVO : listMetaVO) {
            metaMap.put(metaVO.getId(), metaVO);
        }
        // 遍历更新
        for (DataListMetaVO dataListMeta : listDataListMeta) {
            if (metaMap.containsKey(dataListMeta.getId())) {
                metaMap.remove(dataListMeta.getId());
            }
            dataListDal.saveMeta(dataListMeta);

            if(StringUtils.isEmpty(dataListMeta.getFieldName())){
        		dataListMeta.setFieldName("dataListMeta_"+dataListMeta.getId());
        		dataListDal.saveMeta(dataListMeta);
        	}
        }
        // 移除未提交的记录
        List<Long> listId = new ArrayList<>();
        listId.addAll(metaMap.keySet());
        if (listId.size() > 0) {
            deleteMeta(listId);
        }
        // meta改变，清除Record数据
        dataListDal.deleteRecord(dataListId);

        CommonResult result = new CommonResult();
        result.setSuccess(true);
        return result;
    }

    @Override
    public CommonResult deleteMeta(List<Long> listId) {
        CommonResult result = new CommonResult();
        int count = dataListDal.deleteMeta(listId);
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新 TODO
        }
        return result;
    }

    @Override
    public DataListRecordVO getRecord(Long id) {
        return dataListDal.getRecord(id);
    }

    @Override
    public CommonResult queryRecord(DataListRecordQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", dataListDal.queryRecord(query));
        return result;
    }

    @Override
    public CommonResult saveRecord(DataListRecordVO dataListRecord) {
        CommonResult result = new CommonResult();
        int count = dataListDal.saveRecord(dataListRecord);
        if (count > 0) {
            result.getData().put("id", dataListRecord.getId());
            result.setSuccess(true);
            // 通知更新
            DataListsVO dataListVO = dataListDal.get(dataListRecord.getDataListId());
            dataListRecord.setModelId(dataListVO.getModelId());
            dataListRecord.setOpt("update");
            cacheService.publishDataListRecord(dataListRecord);
        }
        return result;
    }

    @Override
    public CommonResult deleteRecord(Long[] id) {
        CommonResult result = new CommonResult();
        DataListRecordVO record = dataListDal.getRecord(id[0]);
        int count = dataListDal.deleteRecord(id);
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            DataListsVO dataListVO = dataListDal.get(record.getDataListId());
            record.setModelId(dataListVO.getModelId());
            record.setOpt("delete");
            cacheService.publishDataListRecord(record);
        }
        return result;
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("data list sub:{}", message);
        DataListsVO dataListsVO = JSON.parseObject(message, DataListsVO.class);
        Map<String, Object> listRecordMap = dataListRecordCacheMap.computeIfAbsent(dataListsVO.getModelId(), k -> new HashMap<>());
        if (dataListsVO.getOpt().equals("delete")) {
            listRecordMap.remove(dataListsVO.getName());
        } else if (dataListsVO.getOpt().equals("new")) {
            Map<String, String> recordsMap = new HashMap<>();
            listRecordMap.put(dataListsVO.getName(), recordsMap);
        }

    }

    @PostConstruct
    public void init() {
        cacheService.subscribeDataList(this);
        new Thread(() ->{

                List<ModelVO> modelList = modelService.listModel(null);
                // 加载系统数据名单列表
                Map<String, Object> sysDataListMap = new HashMap<>();
                List<DataListsVO> sysList = dataListDal.listDataLists(0L, null);
                for (DataListsVO dataListVO : sysList) {
                    Map<String, String> dataListRecords = new HashMap<String, String>();
                    // record list
                    List<DataListRecordVO> recordVOList = dataListDal.listDataRecord(dataListVO.getId());
                    if (recordVOList != null) {
                        for (DataListRecordVO record : recordVOList) {
                            dataListRecords.put(record.getDataRecord(), "");
                        }
                    }
                    sysDataListMap.put(dataListVO.getName(), dataListRecords);
                }


                for (ModelVO model : modelList) {
                    Map<String, Object> dataListMap = new HashMap<>();
                    // datalist list
                    List<DataListsVO> dataLists = dataListDal.listDataLists(model.getId(), null);
                    if (dataLists != null) {
                        for (DataListsVO dataListVO : dataLists) {
                            Map<String, String> dataListRecords = new HashMap<>();
                            // record list
                            List<DataListRecordVO> recordVOList = dataListDal.listDataRecord(dataListVO.getId());
                            if (recordVOList != null) {
                                for (DataListRecordVO record : recordVOList) {
                                    dataListRecords.put(record.getDataRecord(), "");
                                }
                            }
                            dataListMap.put(dataListVO.getName(), dataListRecords);

                        }
                    }


                    // add sys data list
                    dataListMap.putAll(sysDataListMap);
                    dataListRecordCacheMap.put(model.getId(), dataListMap);
                }

                logger.info("data list has loaded.");


        }).start();
    }

    @Override
    public Map<String, Object> getDataListMap(Long modelId) {
        Map<String, Object> listMap = dataListRecordCacheMap.get(modelId);
        return listMap;
    }

    @Override
    public CommonResult batchImportData(List<DataListsVO> list, Long modelId) {
        CommonResult result = new CommonResult();
        for (DataListsVO data : list) {
            data.setStatus(1);
            data.setName("");
            data.setModelId(modelId);
            int count = dataListDal.save(data);
            if (count > 0) {
                if(StringUtils.isEmpty(data.getName())){
                    data.setName("dataList_"+data.getId());
                    dataListDal.save(data);
                }
                // 通知更新
                data.setOpt("new");
                cacheService.publishDataList(data);
            }
        }
        result.setSuccess(true);
        result.setMsg("导入成功");
        return result;
    }

    @Override
    public CommonResult batchImportDataRecord(List<DataListRecordVO> list, Long dataListId) {
        CommonResult result = new CommonResult();
        for (DataListRecordVO dataListRecord : list) {
            dataListRecord.setDataListId(dataListId);
            int count = dataListDal.saveRecord(dataListRecord);
            if (count > 0) {
                // 通知更新
                DataListsVO dataListVO = dataListDal.get(dataListRecord.getDataListId());
                dataListRecord.setModelId(dataListVO.getModelId());
                dataListRecord.setOpt("update");
                cacheService.publishDataListRecord(dataListRecord);
            }
        }
        result.setSuccess(true);
        result.setMsg("导入成功");
        return result;
    }
}
