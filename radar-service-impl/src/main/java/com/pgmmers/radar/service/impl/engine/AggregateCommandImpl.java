package com.pgmmers.radar.service.impl.engine;


import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.service.data.MongoService;
import com.pgmmers.radar.service.engine.AggregateCommand;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AggregateCommandImpl implements AggregateCommand {

    @Autowired
    private MongoService mongoService;

    @Override
    public long count(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin,
            Date end) {
        String collectionName = "entity_" + modelId;
        Long qty = mongoService.count(collectionName, Filters.and(Filters.eq(searchField, searchFieldValue),
                Filters.gte(refDateName, begin.getTime()), Filters.lte(refDateName, end.getTime())));
        return qty;
    }

    @Override
    public long distinctCount(String modelId, String searchField, Object searchFieldValue, String refDateName,
            Date begin, Date end, String distinctBy) {
        String collectionName = "entity_" + modelId;
        Long qty = mongoService.distinctCount(collectionName, Filters.and(Filters.eq(searchField, searchFieldValue),
                Filters.gte(refDateName, begin.getTime()), Filters.lte(refDateName, end.getTime())), distinctBy);
        return qty;
    }

    @Override
    public BigDecimal sum(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin,
            Date end, String funcField) {
        BigDecimal sum = null;
        String collectionName = "entity_" + modelId;
        Document match = new Document("$match", new Document(searchField, searchFieldValue).append(refDateName,
                new Document("$gte", begin.getTime()).append("$lte", end.getTime())));

        Document group = new Document("$group", new Document("_id", null).append("sum", new Document("$sum", "$"
                + funcField)));
        List<Bson> pipeline = Arrays.asList(match, group);
        AggregateIterable<Document> it = mongoService.aggregate(collectionName, pipeline);
        Document doc = it.first();
        if (doc != null) {
            sum = new BigDecimal(doc.get("sum").toString());
        } else {
            sum = BigDecimal.ZERO;
        }
        return sum;
    }

    @Override
    public BigDecimal average(String modelId, String searchField, Object searchFieldValue, String refDateName,
            Date begin, Date end, String funcField) {
        BigDecimal avg = null;
        String collectionName = "entity_" + modelId;
        Document match = new Document("$match", new Document(searchField, searchFieldValue).append(refDateName,
                new Document("$gte", begin.getTime()).append("$lte", end.getTime())));

        Document group = new Document("$group", new Document("_id", null).append("avg", new Document("$avg", "$"
                + funcField)));
        List<Bson> pipeline = Arrays.asList(match, group);
        AggregateIterable<Document> it = mongoService.aggregate(collectionName, pipeline);
        Document doc = it.first();
        if (doc != null) {
            avg = new BigDecimal(doc.get("avg").toString());
        }
        return avg.setScale(2, 4);
    }

    @Override
    public BigDecimal median(String modelId, String searchField, Object searchFieldValue, String refDateName,
            Date begin, Date end, String funcField) {
        BigDecimal avg = null;
        String collectionName = "entity_" + modelId;
        Document match = new Document("$match", new Document(searchField, searchFieldValue).append(refDateName,
                new Document("$gte", begin.getTime()).append("$lte", end.getTime())));

        Document sort = new Document("$sort", new Document(funcField, 1));
        List<Bson> pipeline = Arrays.asList(match, sort);
        AggregateIterable<Document> it = mongoService.aggregate(collectionName, pipeline);
        List<Document> docList = new ArrayList<>();
        it.forEach(new Block<Document>() {

            @Override
            public void apply(Document t) {
                docList.add(t);

            }
        });

        Document doc = null;
        BigDecimal median = null;
        if (docList.size() == 0) {
            return BigDecimal.ZERO;

        } else {
            int model = docList.size() % 2;
            if (model == 1) {
                doc = docList.get(model);
                median = new BigDecimal(doc.get(funcField).toString());
            } else {
                doc = docList.get(model);
                median = new BigDecimal(doc.get(funcField).toString());
                doc = docList.get(model - 1);
                BigDecimal tmp = new BigDecimal(doc.get(funcField).toString());
                median = median.add(tmp).divide(new BigDecimal(2), 2, 4);
            }
        }

        return median;
    }

    @Override
    public BigDecimal max(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin,
            Date end, String funcField) {
        BigDecimal max = null;
        String collectionName = "entity_" + modelId;
        Document match = new Document("$match", new Document(searchField, searchFieldValue).append(refDateName,
                new Document("$gte", begin.getTime()).append("$lte", end.getTime())));

        Document group = new Document("$group", new Document("_id", null).append("max", new Document("$max", "$"
                + funcField)));
        List<Bson> pipeline = Arrays.asList(match, group);
        AggregateIterable<Document> it = mongoService.aggregate(collectionName, pipeline);
        Document doc = it.first();
        if (doc != null) {
            max = new BigDecimal(doc.get("max").toString());
        }
        return max;
    }

    @Override
    public BigDecimal min(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin,
            Date end, String funcField) {
        BigDecimal min = null;
        String collectionName = "entity_" + modelId;
        Document match = new Document("$match", new Document(searchField, searchFieldValue).append(refDateName,
                new Document("$gte", begin.getTime()).append("$lte", end.getTime())));

        Document group = new Document("$group", new Document("_id", null).append("min", new Document("$min", "$"
                + funcField)));
        List<Bson> pipeline = Arrays.asList(match, group);
        AggregateIterable<Document> it = mongoService.aggregate(collectionName, pipeline);
        Document doc = it.first();
        if (doc != null) {
            min = new BigDecimal(doc.get("min").toString());
        }
        return min;
    }

    @Override
    public BigDecimal sd(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin,
            Date end, String funcField, FieldType fieldType) {
        //方差
        BigDecimal variance = variance(modelId, searchField, searchFieldValue, refDateName, begin, end, funcField,
                fieldType);

        //标准差
        double sd = Math.sqrt(variance.doubleValue());
        return new BigDecimal(sd).setScale(2, 4);
    }

    @Override
    public BigDecimal deviation(String modelId, String searchField, Object searchFieldValue, String refDateName,
            Date begin, Date end, String funcField, FieldType fieldType, BigDecimal sourceVal) {
        BigDecimal avg = average(modelId, searchField, searchFieldValue, refDateName, begin, end, funcField);
        //BigDecimal sd = sd(modelId, searchField, searchFieldValue, refDateName, begin, end, funcField, fieldType);
        BigDecimal deviationVal = sourceVal.subtract(avg).abs();
        BigDecimal deviationRate = deviationVal.multiply(new BigDecimal(100)).divide(avg, 2, 4);
        return deviationRate;
    }

    @Override
    public BigDecimal variance(String modelId, String searchField, Object searchFieldValue, String refDateName,
            Date begin, Date end, String funcField, FieldType fieldType) {
        String collectionName = "entity_" + modelId;
        Bson filter = Filters.and(Filters.eq(searchField, searchFieldValue), Filters.gte(refDateName, begin.getTime()),
                Filters.lte(refDateName, end.getTime()));
        FindIterable<Document> findIt = mongoService.find(collectionName, filter);
        List<BigDecimal> records = new ArrayList<>();
        BigDecimal sum = new BigDecimal("0");
        findIt.forEach(new Block<Document>() {
            @Override
            public void apply(Document t) {
                Object obj = t.get(funcField, fieldType.getClazz());
                BigDecimal tmp = new BigDecimal(obj.toString());
                records.add(tmp);
            }
        });
        BigDecimal avg;
        for (BigDecimal val : records) {
            sum = sum.add(val);
        }
        avg = sum.divide(new BigDecimal(records.size()), 2, 4);
        sum = BigDecimal.ZERO;
        // 差平方和
        for (BigDecimal val : records) {
            sum = sum.add(val.subtract(avg).multiply(val.subtract(avg)));
        }
        //方差
        BigDecimal variance = sum.divide(new BigDecimal(records.size()), 2, 4);
        return variance;
    }

}
