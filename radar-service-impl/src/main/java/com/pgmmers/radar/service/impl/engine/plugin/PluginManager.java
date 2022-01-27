package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;


@Deprecated
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
