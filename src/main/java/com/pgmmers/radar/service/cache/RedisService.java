package com.pgmmers.radar.service.cache;

import java.io.Serializable;
import java.util.List;

public interface RedisService {
    /**
     * <p>setex.</p>
     *
     * @param key a {@link String} object.
     * @param val a {@link String} object.
     * @param seconds a int.
     * @return a boolean.
     */
     boolean setex(String key, String val, int seconds);

    /**
     * <p>get.</p>
     *
     * @param key a {@link String} object.
     * @return a {@link String} object.
     */
    Serializable get(String key);


     <T> T get(String key, Class<T> clazz);

    /**
     * <p>set.</p>
     *
     * @param key a {@link String} object.
     * @param val a {@link String} object.
     * @return a boolean.
     */
     boolean set(String key, String val);

     boolean set(String key,  Serializable  val);

    boolean setex(String key,  Serializable  val, int seconds);

    /**
     * <p>del.</p>
     *
     * @param key a {@link String} object.
     */
     void del(String key);

    /**
     * <p>hget.</p>
     *
     * @param key a {@link String} object.
     * @param field a {@link String} object.
     * @return a {@link String} object.
     */
     String hget(String key, String field);

     <T> List<T> hget(String key, Class<T> clazz);

     List hvals(String key);

    /**
     * <p>hset.</p>
     *
     * @param key a {@link String} object.
     * @param field a {@link String} object.
     * @param val a {@link String} object.
     * @return a boolean.
     */
     boolean hset(String key, String field, String val);

     boolean hset(String key, String field, Serializable val);
    
     void publish(String channel, String message);
    
     void subscribe(String channel, SubscribeHandle handle);

    void subscribe(byte[] channel, SubscribeHandle2 handle);

    boolean contains(String token);
}
