package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson.JSON;
import com.pgmmers.radar.dal.model.DataListDal;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.model.DataListRecordService;
import com.pgmmers.radar.service.model.DataListsService;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import com.pgmmers.radar.vo.model.DataListsVO;
import java.util.Map;
import javax.annotation.PostConstruct;
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
    private DataListDal dataListDal;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private DataListsService dataListsService;

    @Override
    public void onMessage(String channel, String message) {
        logger.info("data list record sub:{}", message);
        DataListRecordVO record = JSON.parseObject(message,
                DataListRecordVO.class);
        DataListsVO dataListVO = dataListDal.get(record.getDataListId());
        Map<String, Object> listRecordMap = dataListsService
                .getDataListMap(record.getModelId());
        Map<String, String> dataListRecords = (Map<String, String>) listRecordMap
                .get(dataListVO.getName());
        String opt = record.getOpt();
        if (opt.equals("delete")) {
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
