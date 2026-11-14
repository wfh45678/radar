package com.radar.plugin.demo;

import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.ParamType;
import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.PluginMeta;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 示例插件：敏感数据识别与脱敏。
 *
 * <p>演示要点：</p>
 * <ul>
 *   <li>{@code @PluginMeta} 声明 key 与元信息（key 与类名解耦）；</li>
 *   <li>{@link #paramSchema()} 描述可配置参数（管理端动态渲染表单）；</li>
 *   <li>{@link #outputMeta()} 声明输出列（管理端自动生成响应列配置）；</li>
 *   <li>多列结果以 {@code Map<String,Object>} 返回，abstraction/rules 以
 *       {@code preItems.<destField>.<列名>} 消费。</li>
 * </ul>
 */
@PluginMeta(key = "SENSITIVE_DATA", name = "SENSITIVE_DATA",
        desc = "敏感数据识别与脱敏（手机号/身份证）", type = "JSON")
public class SensitiveDataPlugin implements Plugin {

    private static final Pattern MOBILE = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    private static final Pattern ID_CARD = Pattern.compile("(?<!\\d)\\d{17}[0-9Xx](?!\\d)");

    @Override
    public List<ParamDescriptor> paramSchema() {
        return List.of(
                ParamDescriptor.name("checkMobile", "识别手机号").type(ParamType.BOOLEAN)
                        .defaultValue("true").build(),
                ParamDescriptor.name("checkIdCard", "识别身份证").type(ParamType.BOOLEAN)
                        .defaultValue("true").build(),
                ParamDescriptor.name("mask", "结果脱敏").type(ParamType.BOOLEAN)
                        .defaultValue("false").build());
    }

    @Override
    public List<OutputColumn> outputMeta() {
        return List.of(
                OutputColumn.of("hasMobile", "含手机号", "STRING"),
                OutputColumn.of("hasIdCard", "含身份证", "STRING"),
                OutputColumn.of("masked", "脱敏值", "STRING"));
    }

    @Override
    public PluginResult execute(PluginRequest request, com.radar.plugin.api.PluginContext context) {
        boolean checkMobile = bool(request.getArgs().get("checkMobile"), true);
        boolean checkIdCard = bool(request.getArgs().get("checkIdCard"), true);
        boolean mask = bool(request.getArgs().get("mask"), false);

        Object raw = request.getSourceValues().isEmpty() ? null : request.getSourceValues().get(0);
        String text = raw == null ? "" : String.valueOf(raw);

        boolean hasMobile = checkMobile && MOBILE.matcher(text).find();
        boolean hasIdCard = checkIdCard && ID_CARD.matcher(text).find();

        String masked = text;
        if (mask) {
            if (hasMobile) {
                masked = MOBILE.matcher(masked).replaceAll(m -> m.group().substring(0, 3) + "****" + m.group().substring(7));
            }
            if (hasIdCard) {
                masked = ID_CARD.matcher(masked).replaceAll(m -> m.group().substring(0, 4) + "**********" + m.group().substring(14));
            }
        }

        Map<String, Object> columns = new LinkedHashMap<>();
        columns.put("hasMobile", String.valueOf(hasMobile));
        columns.put("hasIdCard", String.valueOf(hasIdCard));
        columns.put("masked", masked);
        return PluginResult.of(columns);
    }

    private boolean bool(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
