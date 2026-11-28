package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pgmmers.radar.model.query.DataListQuery;
import com.pgmmers.radar.model.query.DataListRecordQuery;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.converter.DataListMetaMapping;
import com.pgmmers.radar.converter.DataListRecordMapping;
import com.pgmmers.radar.converter.DataListsMapping;
import com.pgmmers.radar.mapper.DataListMetaMapper;
import com.pgmmers.radar.mapper.DataListRecordMapper;
import com.pgmmers.radar.mapper.DataListsMapper;
import com.pgmmers.radar.model.entity.DataListMetaPO;
import com.pgmmers.radar.model.entity.DataListRecordPO;
import com.pgmmers.radar.model.entity.DataListsPO;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.service.model.ScenarioService;
import com.pgmmers.radar.model.vo.model.DataListMetaVO;
import com.pgmmers.radar.model.vo.model.DataListRecordVO;
import com.pgmmers.radar.model.vo.model.DataListsVO;
import com.pgmmers.radar.model.vo.model.ScenarioVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DataListsServiceImpl implements DataListsService, SubscribeHandle {

    public static Logger logger = LoggerFactory.getLogger(DataListsServiceImpl.class);

    @Autowired
    private DataListsMapper dataListMapper;
    @Autowired
    private DataListMetaMapper dataListMetaMapper;
    @Autowired
    private DataListRecordMapper dataListRecordMapper;
    @Resource
    private DataListsMapping dataListsMapping;
    @Resource
    private DataListMetaMapping dataListMetaMapping;
    @Resource
    private DataListRecordMapping dataListRecordMapping;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private ScenarioService scenarioService;

    private static Map<Long, Map<String, Object>> dataListRecordCacheMap = new HashMap<>();

    /**
     * 原 DataListDalImpl.listDataLists 逻辑下沉。
     */
    @Override
    public List<DataListsVO> listDataLists(Long scenarioId, Integer status) {
        List<DataListsPO> list = dataListMapper.selectList(
                new LambdaQueryWrapper<DataListsPO>()
                        .eq(DataListsPO::getScenarioId, scenarioId)
                        .eq(status != null, DataListsPO::getStatus, status));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return dataListsMapping.sourceToTarget(list);
    }

    /**
     * 原 DataListDalImpl.listDataRecord 逻辑下沉。
     */
    @Override
    public List<DataListRecordVO> listDataListRecords(Long dataListId) {
        List<DataListRecordPO> list = dataListRecordMapper.selectList(
                new LambdaQueryWrapper<DataListRecordPO>()
                        .eq(DataListRecordPO::getDataListId, dataListId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return dataListRecordMapping.sourceToTarget(list);
    }

    /**
     * 原 DataListDalImpl.get 逻辑下沉。
     */
    @Override
    public DataListsVO get(Long id) {
        DataListsPO dataListsPO = dataListMapper.selectById(id);
        if (dataListsPO != null) {
            return dataListsMapping.sourceToTarget(dataListsPO);
        }
        return null;
    }

    @Override
    public CommonResult list(Long scenarioId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        // 原 DataListDalImpl.list 逻辑下沉：scenarioId + 系统名单(1L)
        List<DataListsPO> list = dataListMapper.selectList(
                new LambdaQueryWrapper<DataListsPO>()
                        .in(DataListsPO::getScenarioId, Arrays.asList(scenarioId, 1L)));
        List<DataListsVO> listVO = CollectionUtils.isEmpty(list)
                ? new ArrayList<>() : dataListsMapping.sourceToTarget(list);
        result.getData().put("list", listVO);
        return result;
    }

    @Override
    public CommonResult query(DataListQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", queryPage(query));
        return result;
    }

    /**
     * 原 DataListDalImpl.query 逻辑下沉：MyBatis-Plus 分页查询。
     */
    private PageResult<DataListsVO> queryPage(DataListQuery query) {
        Page<DataListsPO> p = dataListMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<DataListsPO>()
                        .eq(DataListsPO::getScenarioId, query.getScenarioId())
                        .like(!StringUtils.isEmpty(query.getName()),
                                DataListsPO::getName, query.getName())
                        .eq(!StringUtils.isEmpty(query.getListType()),
                                DataListsPO::getListType, query.getListType())
                        .eq(query.getStatus() != null,
                                DataListsPO::getStatus, query.getStatus()));
        List<DataListsVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : dataListsMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    /**
     * 原 DataListDalImpl.save 逻辑下沉。
     */
    private int saveDataList(DataListsVO datalist) {
        DataListsPO dataListsPO = dataListsMapping.targetToSource(datalist);
        Date sysDate = new Date();
        int count = 0;
        if (datalist.getId() == null) {
            dataListsPO.setCreateTime(sysDate);
            dataListsPO.setUpdateTime(sysDate);
            count = dataListMapper.insert(dataListsPO);
            datalist.setId(dataListsPO.getId());// 返回id
        } else {
            dataListsPO.setUpdateTime(sysDate);
            count = dataListMapper.updateById(dataListsPO);
        }
        return count;
    }

    @Override
    public CommonResult save(DataListsVO dataList) {
        CommonResult result = new CommonResult();
        int count = saveDataList(dataList);
        if (count > 0) {
        	if(StringUtils.isEmpty(dataList.getName())){
        		dataList.setName("dataList_"+dataList.getId());
        		saveDataList(dataList);
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
        DataListsVO dataList = get(id[0]);
        int count = dataListMapper.delete(
                new LambdaQueryWrapper<DataListsPO>().in(DataListsPO::getId, Arrays.asList(id)));
        // TODO 删除关联子表
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            dataList.setOpt("delete");
            cacheService.publishDataList(dataList);
        }
        return result;
    }

    /**
     * 原 DataListDalImpl.getMeta 逻辑下沉。
     */
    @Override
    public DataListMetaVO getMeta(Long id) {
        DataListMetaPO dataListMetaPO = dataListMetaMapper.selectById(id);
        if (dataListMetaPO != null) {
            return dataListMetaMapping.sourceToTarget(dataListMetaPO);
        }
        return null;
    }

    /**
     * 原 DataListDalImpl.listDataMeta 逻辑下沉。
     */
    private List<DataListMetaVO> listDataMeta(Long dataListId) {
        List<DataListMetaPO> list = dataListMetaMapper.selectList(
                new LambdaQueryWrapper<DataListMetaPO>()
                        .eq(DataListMetaPO::getDataListId, dataListId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return dataListMetaMapping.sourceToTarget(list);
    }

    @Override
    public CommonResult listMeta(Long dataListId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", listDataMeta(dataListId));
        return result;
    }

    /**
     * 原 DataListDalImpl.saveMeta 逻辑下沉。
     */
    private int saveMeta(DataListMetaVO dataListMeta) {
        DataListMetaPO dataListMetaPO = dataListMetaMapping.targetToSource(dataListMeta);
        Date sysDate = new Date();
        int count = 0;
        if (dataListMetaPO.getId() == null) {
            dataListMetaPO.setCreateTime(sysDate);
            dataListMetaPO.setUpdateTime(sysDate);
            count = dataListMetaMapper.insert(dataListMetaPO);
            dataListMeta.setId(dataListMetaPO.getId());// 返回id
        } else {
            dataListMetaPO.setUpdateTime(sysDate);
            count = dataListMetaMapper.updateById(dataListMetaPO);
        }
        return count;
    }

    @Override
    public CommonResult saveMeta(List<DataListMetaVO> listDataListMeta) {
        // 现有数据转为map
        Map<Long, DataListMetaVO> metaMap = new HashMap<Long, DataListMetaVO>();
        Long dataListId = listDataListMeta.get(0).getDataListId();
        List<DataListMetaVO> listMetaVO = listDataMeta(dataListId);
        for (DataListMetaVO metaVO : listMetaVO) {
            metaMap.put(metaVO.getId(), metaVO);
        }
        // 遍历更新
        for (DataListMetaVO dataListMeta : listDataListMeta) {
            if (metaMap.containsKey(dataListMeta.getId())) {
                metaMap.remove(dataListMeta.getId());
            }
            saveMeta(dataListMeta);

            if(StringUtils.isEmpty(dataListMeta.getFieldName())){
        		dataListMeta.setFieldName("dataListMeta_"+dataListMeta.getId());
        		saveMeta(dataListMeta);
        	}
        }
        // 移除未提交的记录
        List<Long> listId = new ArrayList<>();
        listId.addAll(metaMap.keySet());
        if (listId.size() > 0) {
            deleteMeta(listId);
        }
        // meta改变，清除Record数据
        deleteRecordByDataListId(dataListId);

        CommonResult result = new CommonResult();
        result.setSuccess(true);
        return result;
    }

    @Override
    public CommonResult deleteMeta(List<Long> listId) {
        CommonResult result = new CommonResult();
        int count = dataListMetaMapper.delete(
                new LambdaQueryWrapper<DataListMetaPO>().in(DataListMetaPO::getId, listId));
        // TODO 删除关联子表
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新 TODO
        }
        return result;
    }

    /**
     * 原 DataListDalImpl.getRecord 逻辑下沉。
     */
    @Override
    public DataListRecordVO getRecord(Long id) {
        DataListRecordPO dataListRecordPO = dataListRecordMapper.selectById(id);
        if (dataListRecordPO != null) {
            return dataListRecordMapping.sourceToTarget(dataListRecordPO);
        }
        return null;
    }

    @Override
    public CommonResult queryRecord(DataListRecordQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        // 原 DataListDalImpl.queryRecord 逻辑下沉
        Page<DataListRecordPO> p = dataListRecordMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<DataListRecordPO>()
                        .eq(DataListRecordPO::getDataListId, query.getDataListId()));
        List<DataListRecordVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : dataListRecordMapping.sourceToTarget(p.getRecords());
        PageResult<DataListRecordVO> pageResult = new PageResult<>((int) p.getCurrent(),
                (int) p.getSize(), (int) p.getTotal(), listVO);
        result.getData().put("page", pageResult);
        return result;
    }

    /**
     * 原 DataListDalImpl.saveRecord 逻辑下沉。
     */
    private int saveDataListRecord(DataListRecordVO dataListRecord) {
        DataListRecordPO dataListRecordPO = dataListRecordMapping.targetToSource(dataListRecord);
        Date sysDate = new Date();
        int count = 0;
        if (dataListRecordPO.getId() == null) {
            dataListRecordPO.setCreateTime(sysDate);
            dataListRecordPO.setUpdateTime(sysDate);
            count = dataListRecordMapper.insert(dataListRecordPO);
            dataListRecord.setId(dataListRecordPO.getId());// 返回id
        } else {
            dataListRecordPO.setUpdateTime(sysDate);
            count = dataListRecordMapper.updateById(dataListRecordPO);
        }
        return count;
    }

    @Override
    public CommonResult saveRecord(DataListRecordVO dataListRecord) {
        CommonResult result = new CommonResult();
        DataListsVO dataListVO = get(dataListRecord.getDataListId());
        if (dataListRecord.getId() != null) {
            // 通知engine 先删除已经存在的，再update.
            DataListRecordVO oldRecord = getRecord(dataListRecord.getId());
            oldRecord.setScenarioId(dataListVO.getScenarioId());
            oldRecord.setOpt("delete");
            cacheService.publishDataListRecord(oldRecord);
        }
        int count = saveDataListRecord(dataListRecord);
        if (count > 0) {
            result.getData().put("id", dataListRecord.getId());
            result.setSuccess(true);
            // 通知更新
            dataListRecord.setScenarioId(dataListVO.getScenarioId());
            dataListRecord.setOpt("update");
            cacheService.publishDataListRecord(dataListRecord);
        }
        return result;
    }

    @Override
    public CommonResult deleteRecord(Long[] id) {
        CommonResult result = new CommonResult();
        DataListRecordVO record = getRecord(id[0]);
        int count = dataListRecordMapper.delete(
                new LambdaQueryWrapper<DataListRecordPO>().in(DataListRecordPO::getId, Arrays.asList(id)));
        // TODO 删除关联子表
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            DataListsVO dataListVO = get(record.getDataListId());
            record.setScenarioId(dataListVO.getScenarioId());
            record.setOpt("delete");
            cacheService.publishDataListRecord(record);
        }
        return result;
    }

    /**
     * 原 DataListDalImpl.deleteRecord(Long dataListId) 逻辑下沉。
     */
    private int deleteRecordByDataListId(Long dataListId) {
        return dataListRecordMapper.delete(
                new LambdaQueryWrapper<DataListRecordPO>()
                        .eq(DataListRecordPO::getDataListId, dataListId));
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("data list sub:{}", message);
        DataListsVO dataListsVO = JSON.parseObject(message, DataListsVO.class);
        Map<String, Object> listRecordMap = dataListRecordCacheMap.computeIfAbsent(dataListsVO.getScenarioId(), k -> new HashMap<>());
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
                List<ScenarioVO> scenarioList = scenarioService.listScenario(null);
                // 加载系统数据名单列表
                Map<String, Object> sysDataListMap = new HashMap<>(32);
                List<DataListsVO> sysList = listDataLists(1L, null);
                buildList2Map(sysDataListMap, sysList);
                for (ScenarioVO model : scenarioList) {
                    Map<String, Object> dataListMap = new HashMap<>(32);
                    // datalist list
                    List<DataListsVO> dataLists = listDataLists(model.getId(), null);
                    if (dataLists != null) {
                        buildList2Map(dataListMap, dataLists);
                    }
                    // add sys data list
                    dataListMap.putAll(sysDataListMap);
                    dataListRecordCacheMap.put(model.getId(), dataListMap);
                }
                logger.info("data list has loaded.");

        }).start();
    }

    private void buildList2Map(Map<String, Object> dataListMap, List<DataListsVO> dataLists) {
        for (DataListsVO dataListVO : dataLists) {
            Map<String, String> dataListRecords = new HashMap<>();
            // record list
            List<DataListRecordVO> recordVOList = listDataListRecords(dataListVO.getId());
            if (recordVOList != null) {
                for (DataListRecordVO record : recordVOList) {
                    dataListRecords.put(record.getDataRecord(), "");
                }
            }
            dataListMap.put(dataListVO.getName(), dataListRecords);

        }
    }

    @Override
    public Map<String, Object> getDataListMap(Long scenarioId) {
        Map<String, Object> listMap = dataListRecordCacheMap.get(scenarioId);
        return listMap;
    }

    @Override
    public CommonResult batchImportData(List<DataListsVO> list, Long scenarioId) {
        CommonResult result = new CommonResult();
        for (DataListsVO data : list) {
            data.setStatus(1);
            data.setName("");
            data.setScenarioId(scenarioId);
            int count = saveDataList(data);
            if (count > 0) {
                if(StringUtils.isEmpty(data.getName())){
                    data.setName("dataList_"+data.getId());
                    saveDataList(data);
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
            int count = saveDataListRecord(dataListRecord);
            if (count > 0) {
                // 通知更新
                DataListsVO dataListVO = get(dataListRecord.getDataListId());
                dataListRecord.setScenarioId(dataListVO.getScenarioId());
                dataListRecord.setOpt("update");
                cacheService.publishDataListRecord(dataListRecord);
            }
        }
        result.setSuccess(true);
        result.setMsg("导入成功");
        return result;
    }
}
