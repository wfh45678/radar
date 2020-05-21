package com.pgmmers.radar.service.impl.engine.Plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.support.SpringFactoriesLoader;


public class PluginManager {

    private PluginManager() {
    }

    public static Map<String, PluginServiceV2> pluginServiceMap() {
        return SingletonHolder.pluginServiceMap;
    }
    private static class SingletonHolder {
        private static final Map<String, PluginServiceV2> pluginServiceMap = SpringFactoriesLoader
                .loadFactories(PluginServiceV2.class, null).stream()
                .sorted(Comparator.comparing(PluginServiceV2::key))
                .collect(Collectors.toMap(PluginServiceV2::pluginName, e -> e,
                        (oldValue, newValue) -> newValue));
        ;
    }
}
