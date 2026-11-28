package com.pgmmers.radar.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pgmmers.radar.common.util.GroovyScriptUtil;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * L2 种子脚本回归：radar-init.sql（2019 基线）中的全部 Groovy 脚本在 Groovy 4 下编译与执行。
 * 这是 Groovy 2.4 → 4.0 升级的兼容性闸门（v2.0 冒烟结论的自动化固化）。
 *
 * <p>脚本格式统一：{@code class StrategyCheckScript / AbstractionCheckScript { boolean check(def data, def lists) }}。</p>
 *
 * <p>断言策略：
 * 编译必须 100% 通过（硬断言）；
 * 执行时对脚本做引用预填（fields→""、abstractions→0、lists→空集合），
 * Groovy 4 API 层破坏（MissingMethodException 等）为硬断言失败，
 * 因合成数据形状不足导致的业务异常仅记录不失败——后者应由 L3 全链路回归覆盖。</p>
 */
class Groovy4SeedScriptTest {

    private static final Path SEED_SQL = Path.of("sql", "radar-init.sql");

    /** 单引号 SQL 字段（处理 \\' 与 \\\\ 转义） */
    private static final Pattern QUOTED = Pattern.compile("'((?:\\\\.|[^'\\\\])*)'");

    private static final Pattern SCRIPT_FIELD = Pattern.compile(
            "class (StrategyCheckScript|AbstractionCheckScript)");

    /** 脚本内 data.xxx.yyy（含更深属性链）引用，用于合成数据预填 */
    private static final Pattern DATA_REF = Pattern.compile("data\\.(\\w+)\\.(\\w+)((?:\\.\\w+)*)");

    private static final Pattern LISTS_REF = Pattern.compile("lists\\.(\\w+)");

    /** Groovy 4 API 层破坏信号：出现即说明升级不兼容，必须硬失败 */
    private static final List<String> API_BREAKAGE = List.of(
            "MissingMethodException", "MissingPropertyException", "GroovyRuntimeException",
            "MissingClassException", "NoSuchMethodError", "NoClassDefFoundError");

    record SeedScript(String sourceTable, int sqlLineNo, String script) {
        String describe() {
            return sourceTable + "#" + sqlLineNo;
        }
    }

    private static List<SeedScript> extractSeedScripts() throws IOException {
        assertTrue(Files.exists(SEED_SQL), "种子 SQL 不存在: " + SEED_SQL.toAbsolutePath());
        List<SeedScript> scripts = new ArrayList<>();
        List<String> lines = Files.readAllLines(SEED_SQL, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith("INSERT INTO `engine_rule`") && !line.startsWith("INSERT INTO `engine_abstraction`")) {
                continue;
            }
            String table = line.startsWith("INSERT INTO `engine_rule`") ? "engine_rule" : "engine_abstraction";
            Matcher matcher = QUOTED.matcher(line);
            while (matcher.find()) {
                String content = unescape(matcher.group(1));
                if (SCRIPT_FIELD.matcher(content).find()) {
                    scripts.add(new SeedScript(table, i + 1, content));
                }
            }
        }
        return scripts;
    }

    /** 还原 Navicat dump 的 SQL 字符串转义 */
    private static String unescape(String sql) {
        Matcher matcher = Pattern.compile("\\\\([nrt'\"\\\\])").matcher(sql);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String replacement = switch (matcher.group(1)) {
                case "n" -> "\n";
                case "r" -> "\r";
                case "t" -> "\t";
                default -> matcher.group(1);
            };
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 按脚本引用预填合成数据：叶子引用 fields/entity → ""、abstractions → 0；
     * 存在更深属性链的引用（如 data.preItems.ipLocationPre.city）预填空 Map——
     * Groovy 对 Map 缺键返回 null 而非抛异常，避免合成数据噪声。
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> syntheticDataFor(String script) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        Map<String, Object> abstractions = new HashMap<>();
        data.put("fields", fields);
        data.put("abstractions", abstractions);
        Matcher dataRefs = DATA_REF.matcher(script);
        while (dataRefs.find()) {
            String bucketName = dataRefs.group(1);
            boolean deep = !dataRefs.group(3).isEmpty();
            Map<String, Object> bucket;
            switch (bucketName) {
                case "abstractions" -> bucket = abstractions;
                case "fields" -> bucket = fields;
                default -> {
                    Map<String, Object> other = (Map<String, Object>) data.get(bucketName);
                    if (other == null) {
                        other = new HashMap<>();
                        data.put(bucketName, other);
                    }
                    bucket = other;
                }
            }
            Object leaf = deep ? new HashMap<>() : ("abstractions".equals(bucketName) ? 0 : "");
            bucket.putIfAbsent(dataRefs.group(2), leaf);
        }
        return data;
    }

    /** 按引用用法预填名单：containsKey 场景给 Map，其余（contains 等）给 Set */
    private static Map<String, Object> syntheticListsFor(String script) {
        Map<String, Object> lists = new HashMap<>();
        Matcher refs = LISTS_REF.matcher(script);
        while (refs.find()) {
            String name = refs.group(1);
            boolean usesMapApi = Pattern.compile("lists\\." + name + "\\.containsKey").matcher(script).find();
            lists.putIfAbsent(name, usesMapApi ? new HashMap<>() : new HashSet<>());
        }
        return lists;
    }

    @Test
    void compileAllSeedScripts() throws IOException {
        List<SeedScript> scripts = extractSeedScripts();
        assertTrue(scripts.size() >= 30, "种子脚本数量异常偏少，提取逻辑可能失效: " + scripts.size());
        List<String> failures = new ArrayList<>();
        for (SeedScript s : scripts) {
            try {
                GroovyObject object = GroovyScriptUtil.loadScript(s.script());
                assertTrue(object != null, s.describe() + " loadScript 返回 null");
            } catch (Throwable t) {
                failures.add(s.describe() + " → " + t);
            }
        }
        assertTrue(failures.isEmpty(), "存在编译失败的种子脚本（Groovy 4 兼容性闸门）:\n" + String.join("\n", failures));
    }

    @Test
    void executeAllSeedScripts() throws IOException {
        List<SeedScript> scripts = extractSeedScripts();
        List<String> apiBreakage = new ArrayList<>();
        List<String> dataShapeIssues = new ArrayList<>();
        for (SeedScript s : scripts) {
            GroovyObject object = GroovyScriptUtil.loadScript(s.script());
            try {
                Map<String, Object> data = syntheticDataFor(s.script());
                Map<String, Object> lists = syntheticListsFor(s.script());
                Object result = object.invokeMethod("check", new Object[]{data, lists});
                if (result != null && !(result instanceof Boolean)) {
                    dataShapeIssues.add(s.describe() + " → check 返回非布尔: " + result.getClass());
                }
            } catch (Throwable t) {
                String name = t.getClass().getSimpleName();
                // 属性缺失若发生在合成占位值（String/Integer）上，属数据形状噪声而非 Groovy 4 破坏
                boolean syntheticReceiver = t instanceof MissingPropertyException
                        && String.valueOf(t.getMessage()).matches(".*(java\\.lang\\.(String|Integer)).*");
                if (!syntheticReceiver
                        && (API_BREAKAGE.stream().anyMatch(name::contains) || t instanceof NoSuchMethodError)) {
                    apiBreakage.add(s.describe() + " → " + t);
                } else {
                    dataShapeIssues.add(s.describe() + " → " + t.getClass().getSimpleName() + ": "
                            + String.valueOf(t.getMessage()).lines().findFirst().orElse(""));
                }
            }
        }
        assertTrue(apiBreakage.isEmpty(), "存在 Groovy 4 API 层不兼容的种子脚本:\n" + String.join("\n", apiBreakage));
        System.out.printf("[Groovy4SeedScriptTest] 共 %d 个种子脚本：编译+执行完成，%d 个合成数据形状不足（由 L3 全链路覆盖）%n",
                scripts.size(), dataShapeIssues.size());
        dataShapeIssues.forEach(issue -> System.out.println("  [data-shape] " + issue));
    }
}
