package com.pgmmers.radar.service.impl.engine.plugin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pgmmers.radar.mapper.PluginRegistryMapper;
import com.pgmmers.radar.model.entity.PluginRegistryPO;
import com.radar.plugin.api.PluginMeta;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 插件生命周期管理（v3.1）：上传校验 → 落盘 →（可选）整代热重载 → 注册表对账，
 * 外加启停（持久化到 engine_plugin_registry）、删除与执行统计快照。
 *
 * <p>校验目标是"传错"而不是"传毒"：结构/SPI/key 冲突检查；
 * 单租户管理员可信模型（同 v3.0 SSRF 立场），字节码审计与 jar 签名校验列后续。</p>
 *
 * @since 3.1.0
 */
@Service
public class PluginAdminService {

    private static final Logger logger = LoggerFactory.getLogger(PluginAdminService.class);

    private static final String SPI_FILE = "META-INF/services/com.radar.plugin.api.Plugin";

    @Value("${radar.plugins-dir:./plugins}")
    private String pluginsDir;

    /** 上传后立即整代热重载；false 时仅落盘并提示"重启生效"。 */
    @Value("${plugin.admin.hot-reload:true}")
    private boolean hotReload;

    /** 旧类加载器宽限关闭毫秒数（in-flight 执行保护 + Windows 文件句柄释放）。 */
    @Value("${plugin.admin.reload-grace-ms:30000}")
    private long reloadGraceMs;

    /** 上传 jar 大小上限（字节）。 */
    @Value("${plugin.admin.max-jar-size:52428800}")
    private long maxJarSize;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private PluginRegistryMapper registryMapper;

    private final MeterRegistry meterRegistry;

    /** 外置插件 key → jar 文件名（upload/sync 时扫描刷新，删除定位文件用）。 */
    private final Map<String, String> jarByPluginKey = new ConcurrentHashMap<>();

    public PluginAdminService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public boolean isHotReload() {
        return hotReload;
    }

    public long getReloadGraceMs() {
        return reloadGraceMs;
    }

    /** 启动对账：注册表 ↔ engine_plugin_registry 双向同步，并推送禁用集到执行链。 */
    @EventListener(ApplicationReadyEvent.class)
    public void syncRegistry() {
        refreshJarIndex();
        Map<String, PluginRegistryPO> rows = allRows();
        Date now = new Date();
        List<PluginEntry> entries = pluginManager.entries();
        Set<String> liveKeys = new HashSet<>();
        for (PluginEntry entry : entries) {
            liveKeys.add(entry.getKey());
            String jarFile = entry.getSource() == PluginEntry.Source.EXTERNAL
                    ? jarByPluginKey.get(entry.getKey()) : null;
            PluginRegistryPO row = rows.get(entry.getKey());
            if (row == null) {
                PluginRegistryPO fresh = new PluginRegistryPO();
                fresh.setPluginKey(entry.getKey());
                fresh.setSource(entry.getSource().name());
                fresh.setJarFile(jarFile);
                fresh.setVersion(entry.getVersion());
                fresh.setProvider(entry.getProvider());
                fresh.setEnabled(1);
                fresh.setCreateTime(now);
                fresh.setUpdateTime(now);
                registryMapper.insert(fresh);
            } else if (changed(row, entry, jarFile)) {
                row.setSource(entry.getSource().name());
                row.setJarFile(jarFile);
                row.setVersion(entry.getVersion());
                row.setProvider(entry.getProvider());
                row.setUpdateTime(now);
                registryMapper.updateById(row);
            }
        }
        // 清理孤儿行（jar 被手动删除 / 内置插件下线后残留）
        for (Map.Entry<String, PluginRegistryPO> e : rows.entrySet()) {
            if (!liveKeys.contains(e.getKey())) {
                registryMapper.deleteById(e.getValue().getId());
                logger.info("plugin registry orphan row removed: {}", e.getKey());
            }
        }
        Set<String> disabled = disabledKeysFromDb();
        pluginManager.setDisabledKeys(disabled);
        logger.info("plugin registry synced: {} plugins, {} disabled", liveKeys.size(), disabled.size());
    }

    /**
     * 上传外置插件 jar：校验（结构/SPI/key 冲突/大小）→ 原子落盘 →
     * 热重载开启时整代重建并即时生效，否则提示"重启生效"。
     *
     * @return keys 本次上传 jar 声明的插件 key；restartRequired 是否需重启生效
     * @throws IllegalArgumentException 校验失败（消息可直接展示给管理员）
     */
    public synchronized Map<String, Object> upload(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String name = original.substring(original.lastIndexOf('/') + 1)
                .substring(original.lastIndexOf("\\") + 1).trim();
        if (!name.toLowerCase().endsWith(".jar")) {
            throw new IllegalArgumentException("仅支持 .jar 文件: " + original);
        }
        if (!name.matches("[A-Za-z0-9][A-Za-z0-9._-]*")) {
            throw new IllegalArgumentException("文件名仅允许字母/数字/点/下划线/中划线: " + original);
        }
        if (file.getSize() > maxJarSize) {
            throw new IllegalArgumentException("jar 超过大小上限 " + (maxJarSize / 1024 / 1024) + "MB: " + name);
        }
        File dir = new File(pluginsDir);
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IllegalStateException("plugins 目录不可写: " + dir.getAbsolutePath());
        }
        File temp = new File(dir, name + "." + System.currentTimeMillis() + ".tmp");
        File target = new File(dir, name);
        try {
            file.transferTo(temp);
            Set<String> keys = scanJar(temp);
            if (keys.isEmpty()) {
                throw new IllegalArgumentException("jar 中未发现任何插件（缺少 " + SPI_FILE + " 或未标注 @PluginMeta）");
            }
            // 与内置 key 冲突：直接拒绝（内置优先，避免管理员误覆盖引擎能力）
            Set<String> builtin = pluginManager.entries().stream()
                    .filter(e -> e.getSource() != PluginEntry.Source.EXTERNAL)
                    .map(PluginEntry::getKey).collect(Collectors.toSet());
            Set<String> clash = new HashSet<>(keys);
            clash.retainAll(builtin);
            if (!clash.isEmpty()) {
                throw new IllegalArgumentException("与内置插件 key 冲突: " + clash);
            }
            // 与其它外置 jar 冲突：拒绝（同文件名重复上传=替换，放行）
            for (File other : listJarFiles(dir)) {
                if (other.getName().equals(name)) {
                    continue;
                }
                Set<String> otherKeys = scanJar(other);
                otherKeys.retainAll(keys);
                if (!otherKeys.isEmpty()) {
                    throw new IllegalArgumentException("与已上传 jar " + other.getName() + " 的插件 key 冲突: " + otherKeys);
                }
            }
            // 原子落盘；Windows 下旧 jar 若仍被宽限期内的旧 loader 持有会失败，明确告知
            try {
                Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "jar 文件被占用（Windows 宽限期内旧版本未释放），请稍后重试或重启引擎: " + name, e);
            }
            boolean restartRequired = false;
            if (hotReload) {
                List<String> registered = pluginManager.rebuildExternal(reloadGraceMs);
                logger.info("hot reload after upload {}: {}", name, registered);
            } else {
                restartRequired = true;
                logger.info("plugin jar stored (restart required): {}", name);
            }
            syncRegistry();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("jar", name);
            result.put("keys", keys);
            result.put("restartRequired", restartRequired);
            return result;
        } finally {
            if (temp.exists() && !temp.delete()) {
                logger.warn("temp file cleanup failed: {}", temp);
            }
        }
    }

    /** 全量插件视图：注册表条目 + 持久化启停状态 + 执行统计快照（管理页数据源）。 */
    public Map<String, Object> list() {
        Map<String, PluginRegistryPO> rows = allRows();
        List<Map<String, Object>> items = new ArrayList<>();
        for (PluginEntry e : pluginManager.entries()) {
            PluginRegistryPO row = rows.get(e.getKey());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", e.getKey());
            item.put("name", e.getName());
            item.put("desc", e.getDesc());
            item.put("type", e.getType());
            item.put("remote", e.isRemote());
            item.put("source", e.getSource().name());
            item.put("version", e.getVersion());
            item.put("provider", e.getProvider());
            item.put("jarFile", e.getSource() == PluginEntry.Source.EXTERNAL
                    ? jarByPluginKey.get(e.getKey()) : null);
            item.put("enabled", row == null || row.getEnabled() == null || row.getEnabled() == 1);
            item.put("paramSchema", e.getParamSchema());
            item.put("outputMeta", e.getOutputMeta());
            item.put("metrics", metricsOf(e.getKey()));
            items.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("plugins", items);
        result.put("config", Map.of(
                "hotReload", hotReload,
                "reloadGraceMs", reloadGraceMs,
                "pluginsDir", new File(pluginsDir).getAbsolutePath()));
        return result;
    }

    /** 启用/禁用插件（持久化 + 即时生效：引擎执行链与 /common/plugins 双侧过滤）。 */
    public synchronized void setEnabled(String key, boolean enabled) {
        PluginEntry entry = pluginManager.entries().stream()
                .filter(e -> e.getKey().equals(key)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("插件不存在: " + key));
        PluginRegistryPO row = allRows().get(key);
        Date now = new Date();
        if (row == null) {
            row = new PluginRegistryPO();
            row.setPluginKey(key);
            row.setSource(entry.getSource().name());
            row.setCreateTime(now);
        }
        row.setEnabled(enabled ? 1 : 0);
        row.setUpdateTime(now);
        if (row.getId() == null) {
            registryMapper.insert(row);
        } else {
            registryMapper.updateById(row);
        }
        pluginManager.setDisabledKeys(disabledKeysFromDb());
        logger.info("plugin {} -> {}", key, enabled ? "enabled" : "disabled");
    }

    /** 删除外置插件：删 jar → 整代重载 → 注册表对账。内置插件拒绝删除。 */
    public synchronized Map<String, Object> delete(String key) throws IOException {
        PluginEntry entry = pluginManager.entries().stream()
                .filter(e -> e.getKey().equals(key)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("插件不存在: " + key));
        if (entry.getSource() != PluginEntry.Source.EXTERNAL) {
            throw new IllegalArgumentException("内置插件不允许删除: " + key);
        }
        String jarFile = jarByPluginKey.get(key);
        Map<String, Object> result = new LinkedHashMap<>();
        boolean fileRemoved = false;
        if (jarFile != null) {
            File jar = new File(pluginsDir, jarFile);
            try {
                fileRemoved = Files.deleteIfExists(jar.toPath());
            } catch (IOException e) {
                // Windows：旧 loader 宽限期内持有句柄，删除失败明确告知
                throw new IllegalStateException(
                        "jar 文件被占用（Windows 宽限期内），请稍后重试或重启后手动删除: " + jarFile, e);
            }
        }
        if (hotReload) {
            pluginManager.rebuildExternal(reloadGraceMs);
        } else {
            result.put("restartRequired", true);
        }
        syncRegistry();
        result.put("jarRemoved", fileRemoved);
        result.put("jar", jarFile);
        return result;
    }

    /** 手动整代重载（上传/删除之外的人工兜底入口）。 */
    public synchronized List<String> reload() {
        List<String> registered = pluginManager.rebuildExternal(reloadGraceMs);
        syncRegistry();
        return registered;
    }

    // ---------- 内部实现 ----------

    /** 扫描 plugins/ 目录建立 key → jar 文件名索引。 */
    private void refreshJarIndex() {
        Map<String, String> index = new HashMap<>();
        File dir = new File(pluginsDir);
        for (File jar : listJarFiles(dir)) {
            try {
                for (String key : scanJar(jar)) {
                    index.putIfAbsent(key, jar.getName());
                }
            } catch (Exception e) {
                logger.warn("jar scan failed (skipped): {}", jar.getName(), e);
            }
        }
        jarByPluginKey.clear();
        jarByPluginKey.putAll(index);
    }

    private List<File> listJarFiles(File dir) {
        File[] jars = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
        return jars == null ? List.of() : List.of(jars);
    }

    /**
     * 提取 jar 声明的插件 key：校验 SPI 声明文件存在，遍历 class 条目
     * （不初始化类，仅读注解，避免插件静态块副作用）。
     *
     * @throws IllegalArgumentException 结构非法（非 jar / 缺 SPI 声明）
     */
    private Set<String> scanJar(File jar) {
        boolean spiFound = false;
        Set<String> keys = new HashSet<>();
        URLClassLoader loader = null;
        try (JarInputStream in = new JarInputStream(new FileInputStream(jar))) {
            loader = new URLClassLoader(new URL[]{jar.toURI().toURL()},
                    PluginManager.class.getClassLoader());
            JarEntry entry;
            while ((entry = in.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (SPI_FILE.equals(name)) {
                    spiFound = true;
                }
                if (name.endsWith(".class") && !name.contains("module-info")
                        && !name.contains("package-info")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    try {
                        Class<?> clazz = Class.forName(className, false, loader);
                        PluginMeta meta = clazz.getAnnotation(PluginMeta.class);
                        if (meta != null && !meta.key().isEmpty()) {
                            keys.add(meta.key());
                        }
                    } catch (Throwable ignore) {
                        // 非插件类或依赖缺失的类：与本契约无关，跳过
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("无法读取 jar: " + jar.getName(), e);
        } finally {
            if (loader != null) {
                try {
                    loader.close();
                } catch (IOException ignore) {
                    // 结构校验路径，句柄释放失败不影响结果
                }
            }
        }
        if (!spiFound) {
            throw new IllegalArgumentException("缺少 SPI 声明 " + SPI_FILE + "（请按插件开发指南打包）: " + jar.getName());
        }
        return keys;
    }

    private Map<String, PluginRegistryPO> allRows() {
        List<PluginRegistryPO> rows = registryMapper.selectList(new QueryWrapper<>());
        Map<String, PluginRegistryPO> byKey = new HashMap<>();
        for (PluginRegistryPO row : rows) {
            byKey.put(row.getPluginKey(), row);
        }
        return byKey;
    }

    private Set<String> disabledKeysFromDb() {
        return allRows().values().stream()
                .filter(r -> r.getEnabled() != null && r.getEnabled() == 0)
                .map(PluginRegistryPO::getPluginKey)
                .collect(Collectors.toSet());
    }

    private boolean changed(PluginRegistryPO row, PluginEntry entry, String jarFile) {
        return !entry.getSource().name().equals(row.getSource())
                || !eq(entry.getVersion(), row.getVersion())
                || !eq(entry.getProvider(), row.getProvider())
                || !eq(jarFile, row.getJarFile());
    }

    private boolean eq(String a, String b) {
        return (a == null ? "" : a).equals(b == null ? "" : b);
    }

    /** 执行统计快照（Micrometer 直读）：total/fail/not_found 计数 + 平均耗时。 */
    private Map<String, Object> metricsOf(String key) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        long success = 0;
        long fail = 0;
        long notFound = 0;
        for (Counter c : meterRegistry.find("radar_plugin_executions_total")
                .tag("plugin", key).counters()) {
            long v = (long) c.count();
            switch (String.valueOf(c.getId().getTag("result"))) {
                case "success" -> success = v;
                case "fail" -> fail = v;
                case "not_found" -> notFound = v;
                default -> {
                    // 未知 result 标签：忽略
                }
            }
        }
        metrics.put("success", success);
        metrics.put("fail", fail);
        metrics.put("notFound", notFound);
        Timer timer = meterRegistry.find("radar_plugin_seconds").tag("plugin", key).timer();
        metrics.put("count", timer == null ? 0L : timer.count());
        metrics.put("avgMillis", timer == null ? 0.0 : timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
        return metrics;
    }
}
