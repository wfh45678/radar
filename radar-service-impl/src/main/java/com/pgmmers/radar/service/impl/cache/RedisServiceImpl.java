package com.pgmmers.radar.service.impl.cache;

import com.alibaba.fastjson.JSON;
import com.pgmmers.radar.service.cache.RedisService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.cache.SubscribeHandle2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


//@Service
public class RedisServiceImpl implements RedisService {
    public static Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Autowired
    private ShardedJedisPool jedisPool;


    public boolean setex(String key, String val, int seconds) {
        ShardedJedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.setex(key, seconds, val);
        } catch (Exception e) {
            logger.warn("redis error", e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return true;
    }


    public String get(String key) {
        ShardedJedis jedis = null;
        String value = null;
        try {
            jedis = jedisPool.getResource();
            value = jedis.get(key);
        } catch (Exception e) {
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return value;
    }


    public void del(String key) {
        ShardedJedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(key);
        } catch (Exception e) {

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    public boolean set(String key, String val) {
        ShardedJedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key, val);
        } catch (Exception e) {
            logger.error("redis set val error" + key + ":" + val);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return true;
    }


    public String hget(String key, String field) {
        ShardedJedis jedis = null;
        String value = null;
        try {
            jedis = jedisPool.getResource();
            value = jedis.hget(key, field);
        } catch (Exception e) {
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return value;
    }


    public boolean hset(String key, String field, String val) {
        ShardedJedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.hset(key, field, val);
        } catch (Exception e) {
            logger.error("redis set val error" + key + ":" + field + ":" + val);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return true;
    }


    @Override
    public boolean set(String key, Serializable val) {
        return set(key, JSON.toJSONString(val));
    }

    @Override
    public boolean setex(String key, Serializable val, int seconds) {
        return false;
    }


    @Override
    public <T> T get(String key, Class<T> clazz) {
        String val = get(key);
        return JSON.parseObject(val, clazz);
    }


    @Override
    public boolean hset(String key, String field, Serializable val) {
        return hset(key, field, JSON.toJSONString(val));
    }


    @Override
    public <T> List<T> hget(String key, Class<T> clazz) {
        List<T> objList = new ArrayList<T>();
        ShardedJedis jedis = null;
        List<String> vals = null;
        try {
            jedis = jedisPool.getResource();
            vals = jedis.hvals(key);
            for (String val : vals) {
                objList.add(JSON.parseObject(val, clazz));
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return objList;
    }

    @Override
    public List hvals(String key) {
        return null;
    }

    public void publish(String channel, String message) {
        ShardedJedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Jedis jedis_ = jedis.getShard(channel);
            jedis_.publish(channel, message);
            logger.info("publish success! channel={},message={}", channel, message);
        } catch (RuntimeException ex) {
            logger.error("publish error! channel={},message={}", channel, message);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void subscribe(String channel, SubscribeHandle handle) {
        ShardedJedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Jedis jedis_ = jedis.getShard(channel);
            //异步操作，jedis.subscribe会阻塞
            new Thread(new Runnable() {

                @Override
                public void run() {
                    JedisPubSub listener = new JedisPubSub() {
                        public void onMessage(String channel, String message) {
                            handle.onMessage(channel, message);
                        };
                    };
                    jedis_.subscribe(listener, channel);//block
                }
            }).start();
            logger.info("subscribe success! channel={}", channel);
        } catch (RuntimeException ex) {
            logger.error("subscribe error! channel={}", channel);
        } finally {
            /*if (jedis != null) {
                jedis.close();
            }*/
        }
    }

    @Override
    public void subscribe(byte[] channel, SubscribeHandle2 handle) {

    }

    @Override
    public boolean contains(String token) {
        String value = get(token);
        return value.equals(token);
    }
}
