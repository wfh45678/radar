package com.pgmmers.radar.service.impl.util;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import java.util.Iterator;
import java.util.List;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * 重新封装类。
 * @author feihu.wang
 * @since 2020.04.15
 */
@Deprecated
//@Component
public class MongodbUtil implements InitializingBean {

    public static MongoTemplate mongoTemplate;

    @Override
    public void afterPropertiesSet() {
        mongoTemplate = (MongoTemplate) BeanUtils.getBean("mongoTemplate");
    }

    public static MongoCollection<Document> getCollection(String collectionName) {
        return mongoTemplate.getCollection(collectionName);
    }

    public static void insert(String collectionName, Document doc) {
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.insertOne(doc);
    }

    public static long count(String collectionName, Bson filter) {
        MongoCollection<Document> collection =  getCollection(collectionName);
        return collection.countDocuments(filter);
    }

    public static long distinctCount(String collectionName, Bson filter, String fieldName) {
        MongoCollection<Document> collection =getCollection(collectionName);
        long count = 0;
        Iterator<BsonValue> it = collection.distinct(fieldName, filter, BsonValue.class).iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    public static AggregateIterable<Document> aggregate(String collectionName, List<Bson> pipeline) {
        MongoCollection<Document> collection = getCollection(collectionName);
        return collection.aggregate(pipeline);
    }

    public static FindIterable<Document> find(String collectionName, Bson filter) {
        MongoCollection<Document> collection = getCollection(collectionName);
        return collection.find(filter);
    }

}
