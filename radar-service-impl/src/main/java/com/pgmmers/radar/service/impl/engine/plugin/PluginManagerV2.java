package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Model
 * @since 2022/1/27 11:29
 */
@Service
public class PluginManagerV2 implements ApplicationContextAware {

    private final Map<String, PluginServiceV2> pluginServiceMap = new ConcurrentHashMap<>();

    public  PluginServiceV2  pluginServiceMap(String key){
        return pluginServiceMap.get(key);
    }

    public Map<String, PluginServiceV2> getPluginServiceMap(){
        return pluginServiceMap;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, PluginServiceV2> beansOfType = applicationContext.getBeansOfType(PluginServiceV2.class);
        pluginServiceMap.putAll(beansOfType);
    }
}
