package com.radar.plugin.demo;

import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.PluginContext;
import com.radar.plugin.api.PluginException;
import com.radar.plugin.api.PluginMeta;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UA 解析插件（v3.5 官方示例）：设备类型 / OS / 浏览器大类 / 微信·支付宝内嵌标识。
 *
 * <p>展示"正则规则表"实现姿势：零依赖自实现简化版——目标是提取风控特征维度
 * （设备聚集、内嵌流量识别），不是精确设备识别库；精确场景请参考 PLUGIN_REGISTRY
 * 的社区专业插件。</p>
 */
@PluginMeta(key = "USER_AGENT_PARSE", name = "UA 解析",
        desc = "User-Agent 解析：设备类型/OS/浏览器大类 + 微信/支付宝内嵌标识（简化版，风控特征用）",
        type = "JSON",
        version = "1.0.0", provider = "radar-official")
public class UserAgentParsePlugin implements Plugin {

    @Override
    public java.util.List<ParamDescriptor> paramSchema() {
        // 无参数：UA 从源字段直接解析
        return java.util.List.of();
    }

    @Override
    public java.util.List<OutputColumn> outputMeta() {
        return java.util.List.of(
                OutputColumn.of("deviceType", "设备类型", "STRING"),
                OutputColumn.of("os", "操作系统", "STRING"),
                OutputColumn.of("browser", "浏览器", "STRING"),
                OutputColumn.of("inWechat", "微信内嵌", "STRING"),
                OutputColumn.of("inAlipay", "支付宝内嵌", "STRING"));
    }

    @Override
    public PluginResult execute(PluginRequest request, PluginContext context) throws PluginException {
        Object raw = request.getSourceValues().isEmpty() ? null : request.getSourceValues().get(0);
        String ua = raw == null ? "" : String.valueOf(raw);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("deviceType", deviceType(ua));
        out.put("os", os(ua));
        out.put("browser", browser(ua));
        out.put("inWechat", String.valueOf(ua.contains("MicroMessenger")));
        out.put("inAlipay", String.valueOf(ua.contains("AlipayClient") || ua.contains("AliApp")));
        return PluginResult.of(out);
    }

    /** 设备类型：iPad/平板平板优先，Android 无 Mobile 视为平板，其余含移动关键词为手机，默认 PC。 */
    private String deviceType(String ua) {
        if (ua.isEmpty()) {
            return "unknown";
        }
        String lower = ua.toLowerCase();
        if (lower.contains("ipad") || lower.contains("tablet")) {
            return "tablet";
        }
        if (lower.contains("iphone") || lower.contains("ipod")
                || (lower.contains("android") && lower.contains("mobile"))) {
            return "mobile";
        }
        if (lower.contains("android")) {
            return "tablet";
        }
        if (lower.contains("mobile") || lower.contains("windows phone")) {
            return "mobile";
        }
        return "pc";
    }

    /** OS 大类。 */
    private String os(String ua) {
        String lower = ua.toLowerCase();
        if (lower.contains("iphone") || lower.contains("ipad") || lower.contains("ipod")) {
            return "iOS";
        }
        if (lower.contains("android")) {
            return "Android";
        }
        if (lower.contains("windows")) {
            return "Windows";
        }
        if (lower.contains("mac os") || lower.contains("macintosh")) {
            return "macOS";
        }
        if (lower.contains("linux")) {
            return "Linux";
        }
        return "其他";
    }

    /** 浏览器大类：内嵌 App 优先于浏览器内核（内嵌标识单独成列，这里给出大类归属）。 */
    private String browser(String ua) {
        String lower = ua.toLowerCase();
        if (lower.contains("micromessenger")) {
            return "微信内置";
        }
        if (lower.contains("alipayclient") || lower.contains("aliapp")) {
            return "支付宝内置";
        }
        if (lower.contains("edg/") || lower.contains("edge")) {
            return "Edge";
        }
        if (lower.contains("firefox") || lower.contains("fxios")) {
            return "Firefox";
        }
        if (lower.contains("qqbrowser")) {
            return "QQ 浏览器";
        }
        if (lower.contains("ucbrowser") || lower.contains("ucweb")) {
            return "UC 浏览器";
        }
        if (lower.contains("chrome") || lower.contains("crios")) {
            return "Chrome";
        }
        if (lower.contains("safari")) {
            return "Safari";
        }
        if (lower.contains("msie") || lower.contains("trident")) {
            return "IE";
        }
        return "其他";
    }
}
