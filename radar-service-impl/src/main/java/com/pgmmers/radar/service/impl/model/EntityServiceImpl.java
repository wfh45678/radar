package com.pgmmers.radar.service.impl.model;

import com.pgmmers.radar.service.data.MongoService;
import com.pgmmers.radar.service.model.EntityService;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.vo.model.ModelVO;
import java.util.Date;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EntityServiceImpl implements EntityService {

    private final Logger logger = LoggerFactory.getLogger(EntityServiceImpl.class);

    private final ModelService modelService;
    private final MongoService mongoService;

    public EntityServiceImpl(ModelService modelService,
            MongoService mongoService) {
        this.modelService = modelService;
        this.mongoService = mongoService;
    }


    @Override
    public Date save(Long modelId, String jsonString, String attachJson,
            boolean isAllowDuplicate) {
        String tmpUrl = "entity_" + modelId;
        Document doc = Document.parse(jsonString);
        Document atta = Document.parse(attachJson);
        ModelVO model = modelService.getModelById(modelId);
        Date radar_ref_datetime = new Date(doc.getLong(model.getReferenceDate()));
        atta.put("radar_ref_datetime", new Date(doc.getLong(model.getReferenceDate())));
        doc.putAll(atta);
        if (!isAllowDuplicate) {
            //设置查询条件
            Document filter = new Document();
            filter.append(model.getEntryName(), doc.get(model.getEntryName()));
            long qty = mongoService.count(tmpUrl, filter);
            if (qty > 0) {
                logger.info("record has already exsit!");
                return null;
            }
        }
        mongoService.insert(tmpUrl, doc);
        return radar_ref_datetime;
    }

}
