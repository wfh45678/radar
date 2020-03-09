package com.pgmmers.radar.service.impl.model;

import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.impl.util.MongodbUtil;
import com.pgmmers.radar.service.model.EntityService;
import com.pgmmers.radar.vo.model.ModelVO;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EntityServiceImpl implements EntityService, SubscribeHandle {

    private Logger logger = LoggerFactory.getLogger(EntityServiceImpl.class);

    @Value("${spring.data.mongodb.uri}")
    private String url;

    @Autowired
    private ModelDal modelDal;

    @Autowired
    private CacheService cacheService;

    private List<ModelVO> modelList = new ArrayList<>();

    @PostConstruct
    public void init() {
        modelList = modelDal.listModel(null);
        cacheService.subscribeModel(this);
    }
    @Override
    public int save(Long modelId, String jsonString, boolean isAllowDuplicate) {
        String tmpUrl = url + ".entity_" + modelId;
        Document doc = Document.parse(jsonString);
        if (!isAllowDuplicate) {
            ModelVO model = null;//cacheService.getModel(modelId);
            for (ModelVO vo : modelList) {
                if (vo.getId().equals(modelId)) {
                    model = vo;
                    break;
                }
            }
            if (model == null) {
                model = modelDal.getModelById(modelId);
            }
            Document filter = new Document();
            filter.append(model.getEntryName(), doc.get(model.getEntryName()));
            long qty = MongodbUtil.count(tmpUrl, filter);
            if (qty > 0) {
                logger.info("record has already exsit!");
                return 1;
            }
        }
        MongodbUtil.insert(tmpUrl, doc);
        return 1;
    }

    @Override
    public List<Object> find(Long modelId, String conds) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public void onMessage(String channel, String message) {
        modelList = modelDal.listModel(null);
    }
    @Override
    public int save(Long modelId, String jsonString, String attachJson,
            boolean isAllowDuplicate) {
        String tmpUrl = url + ".entity_" + modelId;
        Document doc = Document.parse(jsonString);
        Document atta = Document.parse(attachJson);
        ModelVO model = null;//cacheService.getModel(modelId);
        for (ModelVO vo : modelList) {
            if (vo.getId().equals(modelId)) {
                model = vo;
                break;
            }
        }
        if (model == null) {
            model = modelDal.getModelById(modelId);
        }
        atta.put("radar_ref_datetime", new Date(doc.getLong(model.getReferenceDate())));
        doc.putAll(atta);
        if (!isAllowDuplicate) {
            //设置查询条件
            Document filter = new Document();
            filter.append(model.getEntryName(), doc.get(model.getEntryName()));
            long qty = MongodbUtil.count(tmpUrl, filter);
            if (qty > 0) {
                logger.info("record has already exsit!");
                return 1;
            }
        }
        MongodbUtil.insert(tmpUrl, doc);
        return 1;
    }

}
