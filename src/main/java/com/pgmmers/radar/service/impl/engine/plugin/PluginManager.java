package com.pgmmers.radar.service.impl.engine.plugin;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.FeatureProcessor;
import com.radar.plugin.api.FeatureContext;
import com.radar.plugin.api.PluginContext;
import com.radar.plugin.api.FeatureRequest;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.FeatureResponse;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
 *   <li><b>BUILTIN_V3</b>：v3 {@link Plugin} 旧契约插件（Spring bean，{@deprecated} 桥，v5 移除）；</li>
 *   <li><b>BUILTIN_V4</b>：v4 {@link FeatureProcessor} 主契约插件（Spring bean，官方内置实现）；</li>
 *   <li><b>EXTERNAL / EXTERNAL_LEGACY</b>：plugins/ 目录外置 jar（SPI 发现，子优先类加载器隔离；
 *       v4 FeatureProcessor 为主，v3 Plugin 走 legacy 适配并告警）。</li>
 * </ul>
 *
 * <p>v4.1：旧 PluginServiceV2 适配路径移除（主工程已无实现）；SDK 4.0 见 radar-plugin-api
 * （FeatureProcessor 主契约 + 3.x legacy 桥）。</p>
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

    /** 管理端禁用的插件 key（PluginAdminService 从 engine_plugin_registry 装载并推送）。 */
    private volatile Set<String> disabledKeys = Set.of();

    /** 当前外置插件类加载器（v3.1 起可被 rebuildExternal 代际替换）。 */
    private volatile PluginJarClassLoader externalLoader;

    /** 旧 loader 宽限期关闭调度（让 in-flight 执行跑完再释放 jar 文件句柄）。 */
    private final ScheduledExecutorService loaderCloser = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "plugin-loader-closer");
        t.setDaemon(true);
        return t;
    });

    @Autowired
    private PluginContextFactory contextFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, PluginEntry> entries = new ConcurrentHashMap<>();

    /** 按 key 取插件条目；不存在或已被管理端禁用返回 null（引擎侧按隔离策略处理）。 */
    public PluginEntry entry(String key) {
        if (key != null && disabledKeys.contains(key)) {
            return null;
        }
        return entries.get(key);
    }

    /** 全量条目（按 key 排序）。 */
    public List<PluginEntry> entries() {
        return entries.values().stream()
                .sorted(java.util.Comparator.comparing(PluginEntry::getKey))
                .toList();
    }

    /** 非禁用条目（管理端表单渲染侧视图，按 key 排序）。@since 3.1.0 */
    public List<PluginEntry> activeEntries() {
        return entries.values().stream()
                .filter(e -> !disabledKeys.contains(e.getKey()))
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
        // 1) v4 主契约插件（Spring bean）
        applicationContext.getBeansOfType(FeatureProcessor.class)
                .forEach((beanName, processor) -> register(v4Entry(processor, PluginEntry.Source.BUILTIN_V4)));
        // 2) v3 旧契约插件：v5.0 起 legacy 桥已移除，仅支持 FeatureProcessor 契约（见 samples/radar-plugin-demo）
        // 3) plugins/ 目录外置插件
        loadExternalPlugins();
        logger.info("plugin registry ready: {} plugins {}",
                entries.size(),
                entries.values().stream().map(e -> e.getKey() + "(" + e.getSource() + ")").toList());
    }

    private PluginEntry v4Entry(FeatureProcessor processor, PluginEntry.Source source) {
        List<ParamDescriptor> schema = processor.paramSchema();
        return new PluginEntry(
                processor.key(), processor.name(), processor.desc(),
                processor.type(), processor.remote(), source,
                processor.version(), processor.provider(),
                processor.outputMeta(), schema,
                (feature, context, sourceField) -> executeV4(processor, schema, feature, context, sourceField));
    }

    private Object executeV4(FeatureProcessor processor, List<ParamDescriptor> schema, FeatureVO feature,
                             Map<String, Map<String, ?>> context, String[] sourceField) throws Exception {
        Map<String, Object> jsonInfo = (Map<String, Object>) context.get("fields");
        Map<String, Object> features = (Map<String, Object>) context.get("features");
        Map<String, Object> sourceMap = new LinkedHashMap<>();
        List<Object> sourceValues = new ArrayList<>();
        if (sourceField != null) {
            for (String f : sourceField) {
                sourceMap.put(f, jsonInfo == null ? null : jsonInfo.get(f));
                sourceValues.add(jsonInfo == null ? null : jsonInfo.get(f));
            }
        }
        String rawArgs = feature.getDefinition() == null ? null : feature.getDefinition().path("args").asText(null);
        Map<String, Object> typedArgs = parseArgs(schema, rawArgs);
        FeatureContext featureContext = contextFactory.createFeature(processor.key());
        FeatureRequest request = new FeatureRequest(
                feature.getScenarioId(), feature.getFeatureName(),
                sourceMap, sourceValues, typedArgs, rawArgs, jsonInfo, feature,
                features == null ? java.util.Map.of() : features);
        FeatureResponse response = processor.execute(request, featureContext);
        return response == null ? null : response.getPayload();
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

    /** 扫描 plugins/ 目录下的 jar，以子优先类加载器 + SPI 发现 v3 插件。 */
    private void loadExternalPlugins() {
        rebuildExternal(0);
    }

    /**
     * 外置插件整代重载（v3.1）：新 URLClassLoader 覆盖 plugins/ 全部 jar →
     * 清除旧 EXTERNAL 条目 → 注册新条目 → 旧 loader 宽限后 close（释放 jar 文件句柄，
     * 让 Windows 下换/删文件成为可能）。in-flight 的旧插件执行由引擎失败隔离兜底。
     *
     * @param graceMillis 旧 loader 延迟关闭毫秒数
     * @return 本次注册成功的外置插件 key（按注册顺序）
     */
    public synchronized List<String> rebuildExternal(long graceMillis) {
        PluginJarClassLoader oldLoader = externalLoader;
        File dir = new File(pluginsDir);
        if (!dir.isDirectory()) {
            logger.info("plugins dir not found, skip external plugins: {}", dir.getAbsolutePath());
            closeLater(oldLoader, graceMillis);
            return List.of();
        }
        File[] jars = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            logger.info("no external plugin jars in {}", dir.getAbsolutePath());
            closeLater(oldLoader, graceMillis);
            return List.of();
        }
        // 同 key 冲突在 register 内按先注册者为准处理（jar 文件名序即注册序）
        Arrays.sort(jars);
        URL[] urls = Arrays.stream(jars)
                .map(f -> {
                    try {
                        return f.toURI().toURL();
                    } catch (Exception e) {
                        throw new IllegalStateException("bad plugin jar: " + f, e);
                    }
                })
                .toArray(URL[]::new);
        PluginJarClassLoader newLoader = new PluginJarClassLoader(urls, PluginManager.class.getClassLoader());
        // 先收集再换装：同 key 以迭代序（jar 文件名序）先到者为准，跨 jar 冲突告警
        Map<String, PluginEntry> fresh = new LinkedHashMap<>();
        int failed = 0;
        try {
            // v4 主契约
            for (FeatureProcessor processor : ServiceLoader.load(FeatureProcessor.class, newLoader)) {
                try {
                    PluginEntry entry = v4Entry(processor, PluginEntry.Source.EXTERNAL);
                    // 与内置 key 冲突：告警并跳过（内置优先，DB 存量配置不受外置 jar 影响）
                    PluginEntry builtin = entries.get(entry.getKey());
                    if (builtin != null && builtin.getSource() != PluginEntry.Source.EXTERNAL) {
                        logger.warn("external plugin key conflicts with builtin, skip: {} ({})",
                                entry.getKey(), processor.getClass().getName());
                        failed++;
                        continue;
                    }
                    PluginEntry previous = fresh.putIfAbsent(entry.getKey(), entry);
                    if (previous != null) {
                        logger.warn("duplicate external plugin key across jars, keep first: {} ({} vs {})",
                                entry.getKey(), previous, processor.getClass().getName());
                        failed++;
                    }
                } catch (Exception e) {
                    logger.warn("external plugin init failed: {}", processor.getClass().getName(), e);
                    failed++;
                }
            }
            // v3 legacy 契约：v5.0 起不再支持（旧插件 jar 会被下方 FeatureProcessor 扫描自然跳过，
            // 如需继续使用请按 docs/plugin-dev-guide.md 迁移到 FeatureProcessor 契约）
        } catch (Throwable t) {
            // ServiceLoader 迭代中断（坏 jar 等）：注册表不动，关闭新 loader
            logger.error("external plugin scan aborted, keep previous registry", t);
            try {
                newLoader.close();
            } catch (Exception ignore) {
                // 关闭失败仅影响文件句柄释放，不阻断流程
            }
            return List.of();
        }
        // 代际替换：先清旧 EXTERNAL 条目再注册新条目（窗口期内 entry() 返回 null，由隔离策略兜底）
        entries.values().removeIf(e -> e.getSource() == PluginEntry.Source.EXTERNAL);
        fresh.values().forEach(this::register);
        externalLoader = newLoader;
        closeLater(oldLoader, graceMillis);
        logger.info("external plugins reloaded: {} registered, {} failed/skipped, {} jar(s) from {}",
                fresh.size(), failed, jars.length, dir.getAbsolutePath());
        return List.copyOf(fresh.keySet());
    }

    /** 宽限后关闭旧 loader（jar 文件句柄释放）。 */
    private void closeLater(PluginJarClassLoader loader, long graceMillis) {
        if (loader == null) {
            return;
        }
        loaderCloser.schedule(() -> {
            try {
                loader.close();
                logger.info("old plugin classloader closed");
            } catch (Exception e) {
                logger.warn("old plugin classloader close failed (jar handle may stay until restart)", e);
            }
        }, Math.max(0, graceMillis), TimeUnit.MILLISECONDS);
    }

    /** 管理端禁用集推送（PluginAdminService 启动/启停时调用）。 */
    public void setDisabledKeys(Set<String> keys) {
        this.disabledKeys = keys == null ? Set.of() : Set.copyOf(keys);
        logger.info("plugin disabled keys updated: {}", this.disabledKeys);
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
