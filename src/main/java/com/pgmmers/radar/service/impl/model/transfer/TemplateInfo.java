package com.pgmmers.radar.service.impl.model.transfer;

import java.util.List;
import java.util.Map;

/**
 * 模板卡片信息（v3.4）：目录元数据 + 包体解析统计，不含包体本体。
 *
 * @since 3.4.0
 */
public class TemplateInfo {

    /** 模板来源。 */
    public enum Source {
        /** 随 jar 内置。 */
        BUILTIN,
        /** 在线模板源（riskengine.cn 或自配置地址）。 */
        REMOTE
    }

    private String key;
    private String name;
    private String description;
    private List<String> tags;
    private String minRadarVersion;
    private Source source;
    private Map<String, Object> stats;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getMinRadarVersion() {
        return minRadarVersion;
    }

    public void setMinRadarVersion(String minRadarVersion) {
        this.minRadarVersion = minRadarVersion;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }
}
