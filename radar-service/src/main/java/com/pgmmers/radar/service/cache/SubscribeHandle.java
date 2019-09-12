package com.pgmmers.radar.service.cache;

/**
 * 订阅事件处理接口.
 * @author wangfeihu
 *
 */
public interface SubscribeHandle {
    
     void onMessage(String channel, String message);

}
