package com.pgmmers.radar.service.impl.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Iterator;
import java.util.List;

public class MongodbUtil {

    private static MongoClient client = null;

    static MongoClient initClient(String host, int port) {
        if (client == null) {
            client = new MongoClient(host, port);
        }
        return client;
    }

    static MongoClient initClient(String uri) {
        if (client == null) {
            client = new MongoClient(new MongoClientURI(uri));
        }
        return client;
    }

    public static void closeClient(MongoClient client) {
        client.close();
        client = null;
    }

    public static MongoCollection<Document> getCollection(String uri) {
        MongoClientURI clientURI = new MongoClientURI(uri);
        return client.getDatabase(clientURI.getDatabase()).getCollection(clientURI.getCollection());
    }

    public static void insert(String url, Document doc) {
        MongoClientURI uri = new MongoClientURI(url);
        if (client == null) {
            client = new MongoClient(uri);
        }
        MongoCollection<Document> collection = client.getDatabase(uri.getDatabase()).getCollection(uri.getCollection());
        collection.insertOne(doc);
    }

    public static long count(String url, Bson filter) {
        MongoClientURI uri = new MongoClientURI(url);
        if (client == null) {
            client = new MongoClient(uri);
        }
        MongoCollection<Document> collection = client.getDatabase(uri.getDatabase()).getCollection(uri.getCollection());
        long count = collection.count(filter);
        return count;
    }

    public static long distinctCount(String url, Bson filter, String fieldName) {
        MongoClientURI uri = new MongoClientURI(url);
        if (client == null) {
            client = new MongoClient(uri);
        }
        MongoCollection<Document> collection = client.getDatabase(uri.getDatabase()).getCollection(uri.getCollection());
        long count = 0;
        Iterator<BsonValue> it = collection.distinct(fieldName, filter, BsonValue.class).iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    public static AggregateIterable<Document> aggregate(String url, List<Bson> pipeline) {
        MongoClientURI uri = new MongoClientURI(url);
        if (client == null) {
            client = new MongoClient(uri);
        }
        MongoCollection<Document> collection = client.getDatabase(uri.getDatabase()).getCollection(uri.getCollection());
        AggregateIterable<Document> it = collection.aggregate(pipeline);
        return it;
    }
    
    public static FindIterable<Document> find(String url, Bson filter) {
        MongoClientURI uri = new MongoClientURI(url);
        if (client == null) {
            client = new MongoClient(uri);
        }
        MongoCollection<Document> collection = client.getDatabase(uri.getDatabase()).getCollection(uri.getCollection());
        FindIterable<Document> it = collection.find(filter);
        return it;
    }
    
    public static MongoClient getClient(String uri) {
        if (client == null) {
            client = new MongoClient(new MongoClientURI(uri));
        }
        return client;
    }

}
