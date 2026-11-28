package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.model.DataListRecordService;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.model.vo.model.DataListRecordVO;
import com.pgmmers.radar.model.vo.model.DataListsVO;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataListRecordServiceImpl implements DataListRecordService,
        SubscribeHandle {

    public static Logger logger = LoggerFactory
            .getLogger(DataListRecordServiceImpl.class);

    @Autowired
    private CacheService cacheService;
    @Autowired
    private DataListsService dataListsService;

    @Override
    public void onMessage(String channel, String message) {
        logger.info("data list record sub:{}", message);
        DataListRecordVO record = JSON.parseObject(message,
                DataListRecordVO.class);
        DataListsVO dataListVO = dataListsService.get(record.getDataListId());
        Map<String, Object> listRecordMap = dataListsService
                .getDataListMap(record.getScenarioId());
        Map<String, String> dataListRecords = (Map<String, String>) listRecordMap
                .get(dataListVO.getName());
        String opt = record.getOpt();
        if (("delete").equals(opt)) {
            dataListRecords.remove(record.getDataRecord());
        } else {
            dataListRecords.put(record.getDataRecord(), "");
        }
    }

    @PostConstruct
    public void init() {
        cacheService.subscribeDataListRecord(this);
    }

}
