package com.pgmmers.radar.service.impl.cache;

import com.pgmmers.radar.service.cache.RedisService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.cache.SubscribeHandle2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTemplateServiceImpl implements RedisService {
    public static Logger logger = LoggerFactory.getLogger(RedisTemplateServiceImpl.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Override
    public boolean setex(String key, String val, int seconds) {
        stringRedisTemplate.opsForValue().set(key, val, seconds, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public Serializable get(String key) {
        return redisTemplate.opsForValue().get(key);
    }



    @Override
    public <T> T get(String key, Class<T> clazz) {

        return null;
    }

    @Override
    public boolean set(String key, String val) {
         stringRedisTemplate.opsForValue().set(key,val);
         return  true;
    }

    @Override
    public boolean set(String key, Serializable val) {
        redisTemplate.opsForValue().set(key, val);
        return true;
    }

    @Override
    public boolean setex(String key, Serializable val, int seconds) {
        redisTemplate.opsForValue().set(key,val, seconds,TimeUnit.SECONDS);
        return false;
    }

    @Override
    public void del(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public String hget(String key, String field) {
        Object obj = stringRedisTemplate.opsForHash().get(key, field);
        return obj == null? null : obj.toString();
    }

    @Override
    public <T> List<T> hget(String key, Class<T> clazz) {
        List<Object> list = redisTemplate.opsForHash().values(key);
        List<T> list2 = (List<T>) list;
        return list2;
    }

    @Override
    public  List hvals(String key) {
        List<Object> list = redisTemplate.opsForHash().values(key);
        return list;
    }

    @Override
    public boolean hset(String key, String field, String val) {
        stringRedisTemplate.opsForHash().put(key,field, val);
        return true;
    }

    @Override
    public boolean hset(String key, String field, Serializable val) {
        redisTemplate.opsForHash().put(key, field, val);
        return true;
    }

    @Override
    public void publish(String channel, String message) {
        logger.info("publish ! channel={}", channel);
        redisTemplate.getConnectionFactory().getConnection().publish(channel.getBytes(), message.getBytes());
    }

    @Override
    public void subscribe(String channel, SubscribeHandle handle) {
        logger.info("subscribe ! channel={}", channel);
        redisTemplate.getConnectionFactory().getConnection().subscribe((msg,pat) ->{
            byte[] bytes = msg.getBody();
            handle.onMessage(channel, new String(bytes));
        }, channel.getBytes());
        logger.info("subscribe success channel={}", channel);
    }

    @Override
    public void subscribe(byte[] channel, SubscribeHandle2 handle) {
        logger.info("subscribe ! channel={}", new String(channel));

        redisTemplate.getConnectionFactory().getConnection().subscribe((msg,pat) ->{
            byte[] bytes = msg.getBody();
            handle.onMessage(channel,bytes);
        }, channel);
        logger.info("subscribe success! channel={}",  new String(channel));

    }

    @Override
    public boolean contains(String token) {
        return stringRedisTemplate.hasKey(token);
    }
}
