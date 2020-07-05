package com.pgmmers.radar.service.data;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Iterator;
import java.util.List;

/**
 * mongo db access entry.
 *
 * @author feihu.wang
 * @since 2020-04-15
 */
public interface MongoService {

    MongoTemplate getMongoTemplate();

    default MongoCollection<Document> getCollection(String collectionName) {
        return getMongoTemplate().getCollection(collectionName);
    }

    default void insert(String collectionName, Document doc) {
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.insertOne(doc);
    }

    default long count(String collectionName, Bson filter) {
        MongoCollection<Document> collection = getCollection(collectionName);
        return collection.countDocuments(filter);
    }

    default long distinctCount(String collectionName, Bson filter, String fieldName) {
        MongoCollection<Document> collection = getCollection(collectionName);
        long count = 0;
        Iterator<BsonValue> it = collection.distinct(fieldName, filter, BsonValue.class).iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    default AggregateIterable<Document> aggregate(String collectionName, List<Bson> pipeline) {
        MongoCollection<Document> collection = getCollection(collectionName);
        return collection.aggregate(pipeline);
    }

    default FindIterable<Document> find(String collectionName, Bson filter) {
        MongoCollection<Document> collection = getCollection(collectionName);
        return collection.find(filter);
    }
}
