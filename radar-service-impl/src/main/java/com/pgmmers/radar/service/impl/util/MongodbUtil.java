package com.pgmmers.radar.service.impl.util;

import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongodbUtil implements InitializingBean {

    public static MongoTemplate mongoTemplate;

    @Override
    public void afterPropertiesSet() {
        mongoTemplate = (MongoTemplate) BeanUtils.getBean("mongoTemplate");
    }

    public static MongoCollection<Document> getCollection(String uri) {
        MongoClientURI clientURI = new MongoClientURI(uri);
        return mongoTemplate.getCollection(Objects.requireNonNull(clientURI.getCollection()));
    }

    public static void insert(String url, Document doc) {
        MongoCollection<Document> collection = getCollection(url);
        collection.insertOne(doc);
    }

    public static long count(String url, Bson filter) {
        MongoCollection<Document> collection =  getCollection(url);
        long count = collection.count(filter);
        return count;
    }

    public static long distinctCount(String url, Bson filter, String fieldName) {
        MongoCollection<Document> collection =getCollection(url);
        long count = 0;
        Iterator<BsonValue> it = collection.distinct(fieldName, filter, BsonValue.class).iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    public static AggregateIterable<Document> aggregate(String url, List<Bson> pipeline) {
        MongoCollection<Document> collection = getCollection(url);
        AggregateIterable<Document> it = collection.aggregate(pipeline);
        return it;
    }

    public static FindIterable<Document> find(String url, Bson filter) {
        MongoCollection<Document> collection = getCollection(url);
        FindIterable<Document> it = collection.find(filter);
        return it;
    }

}
