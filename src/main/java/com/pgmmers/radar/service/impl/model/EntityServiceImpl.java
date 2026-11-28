package com.pgmmers.radar.service.impl.model;

import com.pgmmers.radar.service.data.MongoService;
import com.pgmmers.radar.service.model.EntityService;
import com.pgmmers.radar.service.model.ScenarioService;
import com.pgmmers.radar.model.vo.model.ScenarioVO;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class EntityServiceImpl implements EntityService {

    private Logger logger = LoggerFactory.getLogger(EntityServiceImpl.class);

    @Resource
    private ScenarioService scenarioService;
    @Resource
    private MongoService mongoService;


    @Override
    public int save(Long scenarioId, String jsonString, boolean isAllowDuplicate) {
        String tmpUrl = "entity_" + scenarioId;
        Document doc = Document.parse(jsonString);
        if (!isAllowDuplicate) {
            ScenarioVO model = scenarioService.getScenarioById(scenarioId);
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
    public List<Object> find(Long scenarioId, String conds) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long update(Long scenarioId, String eventId, String status) {
        ScenarioVO model = scenarioService.getScenarioById(scenarioId);
        Document filter = new Document();
        filter.append(model.getEntryName(), eventId);
        Document updateDoc = new Document();
        updateDoc.append("status", status);
        String collectionName = buildCollectionName(scenarioId);
        return mongoService.update(collectionName, filter, updateDoc);
    }

    @Override
    public int save(Long scenarioId, String jsonString, String attachJson,
            boolean isAllowDuplicate) {
        String collectionName = buildCollectionName(scenarioId);
        Document doc = Document.parse(jsonString);
        Document attach = Document.parse(attachJson);
        ScenarioVO model = scenarioService.getScenarioById(scenarioId);
        attach.put("radar_ref_datetime", new Date(doc.getLong(model.getReferenceDate())));
        attach.put("status", "1");
        doc.putAll(attach);
        if (!isAllowDuplicate) {
            //设置查询条件
            Document filter = new Document();
            filter.append(model.getEntryName(), doc.get(model.getEntryName()));
            long qty = mongoService.count(collectionName, filter);
            if (qty > 0) {
                logger.info("record has already exsit!");
                return 1;
            }
        }
        mongoService.insert(collectionName, doc);
        return 1;
    }

    private String buildCollectionName(Long scenarioId) {
        return "entity_" + scenarioId;
    }

}
