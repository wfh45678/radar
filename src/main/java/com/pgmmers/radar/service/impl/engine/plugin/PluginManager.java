package com.pgmmers.radar.service.impl.engine.plugin;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmmers.radar.model.vo.model.PreItemVO;
import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.PluginContext;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * 插件注册表（v3.0 重构）：归一管理三路插件，引擎执行链只面向 {@link PluginEntry}。
 *
 * <ul>
 *   <li><b>BUILTIN_V2</b>：{@link PluginServiceV2} 旧接口插件（Spring bean，适配保留，存量 key 兼容）；</li>
 *   <li><b>BUILTIN_V3</b>：v3 {@link Plugin} 新接口插件（Spring bean，官方内置实现）；</li>
 *   <li><b>EXTERNAL</b>：plugins/ 目录外置 jar 中的 v3 插件（SPI 发现，子优先类加载器隔离）。</li>
 * </ul>
 *
 * <p>注册顺序 = 冲突优先级：内置先注册，外置后注册，同 key 以先注册者为准并告警。</p>
 *
 * @since 3.0.0
 */
@Service
public class PluginManager implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    /** 外置插件 jar 与引擎共享的类（parent-first 加载，避免多份副本）。 */
    private static final Set<String> PARENT_FIRST_PREFIXES = Set.of(
            "java.", "javax.", "jdk.", "sun.", "com.sun.",
            "com.radar.plugin.api.", "org.slf4j.");

    @Value("${radar.plugins-dir:./plugins}")
    private String pluginsDir;

    @Autowired
    private PluginContextFactory contextFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, PluginEntry> entries = new ConcurrentHashMap<>();

    /** 按 key 取插件条目；不存在返回 null（引擎侧按隔离策略处理）。 */
    public PluginEntry entry(String key) {
        return entries.get(key);
    }

    /** 全量条目（按 key 排序）。 */
    public List<PluginEntry> entries() {
        return entries.values().stream()
                .sorted(java.util.Comparator.comparing(PluginEntry::getKey))
                .toList();
    }

    /** 注册条目：同 key 以先注册者为准。供容器初始化与测试使用。 */
    public void register(PluginEntry entry) {
        PluginEntry existing = entries.putIfAbsent(entry.getKey(), entry);
        if (existing != null) {
            logger.warn("plugin key conflict, keep first: {} (kept={}, skipped source={})",
                    entry.getKey(), existing.getSource(), entry.getSource());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 1) v2 旧接口插件（适配保留，key=类简名，存量 DB 配置兼容）
        applicationContext.getBeansOfType(PluginServiceV2.class)
                .forEach((beanName, v2) -> register(v2Entry(v2)));
        // 2) v3 新接口插件（Spring bean）
        applicationContext.getBeansOfType(Plugin.class)
                .forEach((beanName, plugin) -> register(v3Entry(plugin, PluginEntry.Source.BUILTIN_V3)));
        // 3) plugins/ 目录外置插件
        loadExternalPlugins();
        logger.info("plugin registry ready: {} plugins {}",
                entries.size(),
                entries.values().stream().map(e -> e.getKey() + "(" + e.getSource() + ")").toList());
    }

    private PluginEntry v2Entry(PluginServiceV2 v2) {
        List<OutputColumn> outputMeta = parseLegacyMeta(v2.getMeta());
        return new PluginEntry(
                v2.pluginName(), v2.pluginName(), v2.desc(),
                v2.getType() == null ? "" : v2.getType(),
                false, PluginEntry.Source.BUILTIN_V2,
                outputMeta, List.of(),
                (item, jsonInfo, sourceField) -> v2.handle(item, jsonInfo, sourceField));
    }

    private PluginEntry v3Entry(Plugin plugin, PluginEntry.Source source) {
        List<ParamDescriptor> schema = plugin.paramSchema();
        return new PluginEntry(
                plugin.key(), plugin.name(), plugin.desc(),
                plugin.type(), plugin.remote(), source,
                plugin.outputMeta(), schema,
                (item, jsonInfo, sourceField) -> executeV3(plugin, schema, item, jsonInfo, sourceField));
    }

    private Object executeV3(Plugin plugin, List<ParamDescriptor> schema, PreItemVO item,
                             Map<String, Object> jsonInfo, String[] sourceField) throws Exception {
        Map<String, Object> sourceMap = new LinkedHashMap<>();
        List<Object> sourceValues = new ArrayList<>();
        if (sourceField != null) {
            for (String f : sourceField) {
                sourceMap.put(f, jsonInfo.get(f));
                sourceValues.add(jsonInfo.get(f));
            }
        }
        Map<String, Object> typedArgs = parseArgs(schema, item.getArgs());
        PluginContext context = contextFactory.create(plugin.key());
        PluginRequest request = new PluginRequest(
                item.getModelId(), item.getDestField(),
                sourceMap, sourceValues, typedArgs, item.getArgs(), jsonInfo, item);
        PluginResult result = plugin.execute(request, context);
        return result == null ? null : result.getPayload();
    }

    /**
     * 按 schema 把管理端存储的 args 解析为类型化参数。
     * v3 约定：schema 非空时 args 为 JSON 对象字符串；解析失败（存量行为：
     * args 为自由字符串如 "0,7"、url、格式串）返回空 Map，由插件自行兜底。
     */
    private Map<String, Object> parseArgs(List<ParamDescriptor> schema, String rawArgs) {
        if (schema == null || schema.isEmpty() || rawArgs == null || rawArgs.isBlank()) {
            return Map.of();
        }
        Map<String, Object> raw;
        try {
            raw = objectMapper.readValue(rawArgs, Map.class);
        } catch (Exception e) {
            // 存量自由字符串格式：交给插件 legacy 兜底
            return Map.of();
        }
        if (raw == null) {
            return Map.of();
        }
        Map<String, Object> typed = new LinkedHashMap<>();
        for (ParamDescriptor descriptor : schema) {
            Object value = raw.get(descriptor.getName());
            if (value == null || "".equals(value)) {
                value = descriptor.getDefaultValue();
            }
            if (value instanceof String s && descriptor.getType() != com.radar.plugin.api.ParamType.STRING) {
                value = descriptor.getType().parse(s);
            }
            if (value != null) {
                typed.put(descriptor.getName(), value);
            }
        }
        return typed;
    }

    /** 解析 v2 getMeta() 的 JSON 字符串为 OutputColumn 列表（失败返回空列表）。 */
    private List<OutputColumn> parseLegacyMeta(String metaJson) {
        if (metaJson == null || metaJson.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> rows = JSON.parseObject(metaJson, List.class);
            List<OutputColumn> columns = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                columns.add(OutputColumn.of(
                        String.valueOf(row.get("column")),
                        String.valueOf(row.get("title")),
                        String.valueOf(row.getOrDefault("type", "STRING"))));
            }
            return columns;
        } catch (Exception e) {
            logger.warn("legacy getMeta parse failed", e);
            return List.of();
        }
    }

    /** 扫描 plugins/ 目录下的 jar，以子优先类加载器 + SPI 发现 v3 插件。 */
    private void loadExternalPlugins() {
        File dir = new File(pluginsDir);
        if (!dir.isDirectory()) {
            logger.info("plugins dir not found, skip external plugins: {}", dir.getAbsolutePath());
            return;
        }
        File[] jars = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            logger.info("no external plugin jars in {}", dir.getAbsolutePath());
            return;
        }
        URL[] urls = Arrays.stream(jars)
                .map(f -> {
                    try {
                        return f.toURI().toURL();
                    } catch (Exception e) {
                        throw new IllegalStateException("bad plugin jar: " + f, e);
                    }
                })
                .toArray(URL[]::new);
        ClassLoader pluginLoader = new PluginJarClassLoader(urls, PluginManager.class.getClassLoader());
        int count = 0;
        for (Plugin plugin : ServiceLoader.load(Plugin.class, pluginLoader)) {
            try {
                register(v3Entry(plugin, PluginEntry.Source.EXTERNAL));
                count++;
            } catch (Exception e) {
                logger.warn("external plugin init failed: {}", plugin.getClass().getName(), e);
            }
        }
        logger.info("loaded {} external plugin jar(s) from {}", count, dir.getAbsolutePath());
    }

    /**
     * 子优先类加载器：插件 jar 自带依赖优先从 jar 解析（避免与引擎版本冲突），
     * JDK 与插件契约（api/slf4j）走 parent 共享。
     */
    static final class PluginJarClassLoader extends URLClassLoader {

        PluginJarClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    if (PARENT_FIRST_PREFIXES.stream().anyMatch(name::startsWith)) {
                        c = super.loadClass(name, false);
                    } else {
                        try {
                            c = findClass(name);
                        } catch (ClassNotFoundException e) {
                            c = super.loadClass(name, false);
                        }
                    }
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
    }
}
