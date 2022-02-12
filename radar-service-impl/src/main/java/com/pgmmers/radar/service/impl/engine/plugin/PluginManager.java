package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class PluginManager implements ApplicationContextAware {

//    private PluginManager() {
//    }

//    public static Map<String, PluginServiceV2> pluginServiceMap() {
//        return SingletonHolder.pluginServiceMap;
//    }


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

    // 工厂模式实现单例存在不能注入bean问题
 /*   private static class SingletonHolder {
        private static final Map<String, PluginServiceV2> pluginServiceMap = SpringFactoriesLoader
                .loadFactories(PluginServiceV2.class, null).stream()
                .sorted(Comparator.comparing(PluginServiceV2::key))
                .collect(Collectors.toMap(PluginServiceV2::pluginName, e -> e,
                        (oldValue, newValue) -> newValue));
        ;
    }*/
}
