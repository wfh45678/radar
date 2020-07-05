package com.pgmmers.radar.service.impl.model;

import com.pgmmers.radar.service.data.MongoService;
import com.pgmmers.radar.service.model.EntityService;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.vo.model.ModelVO;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityServiceImpl implements EntityService {

    private Logger logger = LoggerFactory.getLogger(EntityServiceImpl.class);

    @Autowired
    private ModelService modelService;
    @Autowired
    private MongoService mongoService;


    @Override
    public int save(Long modelId, String jsonString, boolean isAllowDuplicate) {
        String tmpUrl = "entity_" + modelId;
        Document doc = Document.parse(jsonString);
        if (!isAllowDuplicate) {
            ModelVO model = modelService.getModelById(modelId);
            Document filter = new Document();
            filter.append(model.getEntryName(), doc.get(model.getEntryName()));
            long qty = mongoService.count(tmpUrl, filter);
            if (qty > 0) {
                logger.info("record has already exsit!");
                return 1;
            }
        }
        mongoService.insert(tmpUrl, doc);
        return 1;
    }

    @Override
    public List<Object> find(Long modelId, String conds) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int save(Long modelId, String jsonString, String attachJson,
            boolean isAllowDuplicate) {
        String tmpUrl = "entity_" + modelId;
        Document doc = Document.parse(jsonString);
        Document atta = Document.parse(attachJson);
        ModelVO model = modelService.getModelById(modelId);
        atta.put("radar_ref_datetime", new Date(doc.getLong(model.getReferenceDate())));
        doc.putAll(atta);
        if (!isAllowDuplicate) {
            //设置查询条件
            Document filter = new Document();
            filter.append(model.getEntryName(), doc.get(model.getEntryName()));
            long qty = mongoService.count(tmpUrl, filter);
            if (qty > 0) {
                logger.info("record has already exsit!");
                return 1;
            }
        }
        mongoService.insert(tmpUrl, doc);
        return 1;
    }

}
