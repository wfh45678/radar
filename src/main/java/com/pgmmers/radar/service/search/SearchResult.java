package com.pgmmers.radar.service.search;

import java.util.ArrayList;
import java.util.List;

/**
 * 检索结果模型（v5.2 ES 8 升级）：解耦 ES 类型——
 * sources 为文档 _source 的 JSON 字符串列表（保持与既有 fastjson2 解析链路兼容）。
 */
public class SearchResult {

    private final long total;
    private final List<String> sources;

    public SearchResult(long total, List<String> sources) {
        this.total = total;
        this.sources = sources == null ? new ArrayList<>() : sources;
    }

    public long getTotal() {
        return total;
    }

    public List<String> getSources() {
        return sources;
    }
}
