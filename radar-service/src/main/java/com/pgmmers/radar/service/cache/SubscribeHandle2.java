package com.pgmmers.radar.service.cache;

/**
 * 订阅事件处理接口.
 * @author wangfeihu
 *
 */
public interface SubscribeHandle2 {
    
     void onMessage(byte[] channel, byte[] message);

}
