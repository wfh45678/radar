package com.pgmmers.radar.service.impl.engine.plugin;

import com.alibaba.fastjson2.JSONObject;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.ParamType;
import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.PluginMeta;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import com.radar.plugin.api.PluginException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 通用 HTTP 取数插件（数据服务厂商接入的现有形态）。
 *
 * <p>v3 迁移：url/reqType 进参数 schema（管理端动态表单），改走引擎 HTTP 工具
 * （强制超时 + 私网防护 + 连接复用），声明 remote 型；url 支持
 * {@code {字段名}} 占位符由源字段值填充；存量配置（args=纯 url、reqType 列）兼容。</p>
 */
@Component
@PluginMeta(key = "HTTP_UTIL", name = "HTTP_UTIL", desc = "HttpUtil", type = "JSON", remote = true)
public class HTTP_UTIL implements Plugin {

    @Override
    public List<ParamDescriptor> paramSchema() {
        return List.of(
                ParamDescriptor.name("url", "请求URL").type(ParamType.STRING).required()
                        .placeholder("https://api.example.com/data?ip={ip}").build(),
                ParamDescriptor.name("reqType", "请求方式").type(ParamType.STRING)
                        .defaultValue("GET").placeholder("GET | POST").build());
    }

    @Override
    public PluginResult execute(PluginRequest request, com.radar.plugin.api.PluginContext context)
            throws PluginException {
        String url;
        String reqType;
        Map<String, Object> args = request.getArgs();
        if (!args.isEmpty()) {
            // v3：schema 化参数（JSON args）
            url = String.valueOf(args.get("url"));
            reqType = String.valueOf(args.getOrDefault("reqType", "GET"));
        } else {
            // 存量兼容：args=纯 url，reqType 在 preItem 配置列
            url = request.getRawArgs();
            reqType = request.getConfig() instanceof com.pgmmers.radar.model.vo.model.PreItemVO item
                    ? item.getReqType() : "GET";
        }
        if (url == null || url.isBlank()) {
            throw new PluginException("HTTP_UTIL 缺少请求 URL");
        }
        // {字段名} 占位符用源字段值填充
        for (Map.Entry<String, Object> entry : request.getSourceFields().entrySet()) {
            url = url.replace("{" + entry.getKey() + "}",
                    entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }

        com.radar.plugin.api.HttpResult response;
        if ("POST".equalsIgnoreCase(reqType)) {
            response = context.http().postJson(url, "{}");
        } else {
            response = context.http().get(url);
        }
        if (!response.isOk()) {
            throw new PluginException("HTTP " + response.getStatus() + ": " + url);
        }
        return PluginResult.of(JSONObject.parseObject(response.getBody()));
    }
}
