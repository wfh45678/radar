package com.radar.plugin.demo.remote;

import com.radar.plugin.api.HttpResult;
import com.radar.plugin.api.KeyValueCache;
import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.ParamOption;
import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.PluginContext;
import com.radar.plugin.api.PluginException;
import com.radar.plugin.api.PluginMeta;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 远程插件官方示例（v3.1）：汇率换算。
 *
 * <p>演示远程插件的完整形态，与 radar-plugin-demo（本地形态）互补：</p>
 * <ul>
 *   <li>{@code remote=true}：引擎据此施加 HTTP 强制超时（{@code plugin.http.request-timeout-ms}）；</li>
 *   <li>{@code paramSchema} 带 {@code options}：管理端渲染为下拉框（v3.1 新增）；</li>
 *   <li>{@code context.cache}：汇率数据 10 分钟缓存，外呼量大降；</li>
 *   <li>{@code outputMeta} 多列输出：RATE / CONVERTED 两列，管理端自动生成响应列配置；</li>
 *   <li>业务失败抛 {@code PluginException}（引擎隔离并计数，不影响事件链路）。</li>
 * </ul>
 *
 * <p>数据源：open.er-api.com 免费汇率接口（无需 key。公共免费服务仅作演示，
 * 生产环境请替换为自有数据服务）。JSON 解析用正则取数以保持插件零依赖——
 * 实际项目建议自带 JSON 库打进插件 jar（子优先类加载器隔离，不与引擎冲突）。</p>
 */
@PluginMeta(key = "EXCHANGE_RATE", name = "汇率换算",
        desc = "金额按实时汇率换算（外呼免费汇率接口，10 分钟缓存）",
        type = "JSON", remote = true,
        version = "1.0.0", provider = "radar-official")
public class ExchangeRatePlugin implements Plugin {

    /** 汇率接口（源币种拼在路径上）。 */
    static final String API_URL = "https://open.er-api.com/v6/latest/";

    /** 扁平 JSON 取数："USD":1.0 形态。 */
    private static final Pattern RATE_PATTERN =
            Pattern.compile("\"([A-Z]{3})\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

    @Override
    public List<ParamDescriptor> paramSchema() {
        return List.of(
                ParamDescriptor.name("from", "源币种").required()
                        .options(ParamOption.of("人民币", "CNY"), ParamOption.of("美元", "USD"),
                                ParamOption.of("欧元", "EUR"))
                        .defaultValue("CNY")
                        .placeholder("金额的币种").build(),
                ParamDescriptor.name("to", "目标币种").required()
                        .options(ParamOption.of("美元", "USD"), ParamOption.of("人民币", "CNY"),
                                ParamOption.of("欧元", "EUR"), ParamOption.of("日元", "JPY"))
                        .defaultValue("USD")
                        .placeholder("换算目标币种").build());
    }

    @Override
    public List<OutputColumn> outputMeta() {
        return List.of(
                OutputColumn.of("RATE", "汇率", "DOUBLE"),
                OutputColumn.of("CONVERTED", "换算金额", "DOUBLE"));
    }

    @Override
    public PluginResult execute(PluginRequest request, PluginContext context) throws PluginException {
        Object amount = request.getSourceFields().isEmpty() ? null
                : request.getSourceValues().get(0);
        if (amount == null || String.valueOf(amount).isBlank()) {
            throw new PluginException("源字段取值为空，无法换算");
        }
        String from = String.valueOf(request.getArgs().getOrDefault("from", "CNY"));
        String to = String.valueOf(request.getArgs().getOrDefault("to", "USD"));

        // 汇率表 10 分钟缓存：以源币种为 key，外呼收敛。
        // KeyValueCache 的 loader 是 Function（不能抛受检异常）：
        // PluginException 先转运行时透传，get 返回后立刻转回 PluginException（保消息，隔离语义不变）
        KeyValueCache cache = context.cache("exchange-rate", Duration.ofMinutes(10));
        Map<String, Double> rates;
        try {
            rates = cache.get(from, base -> {
                try {
                    return fetchRates(base, context);
                } catch (PluginException e) {
                    throw new FetchFailure(e.getMessage(), e);
                }
            });
        } catch (FetchFailure e) {
            throw new PluginException(e.getMessage());
        }

        Double rate = rates.get(to);
        if (rate == null) {
            throw new PluginException("汇率表缺少目标币种: " + to);
        }
        try {
            double converted = Double.parseDouble(String.valueOf(amount)) * rate;
            return PluginResult.of(Map.of("RATE", rate, "CONVERTED", converted));
        } catch (NumberFormatException e) {
            throw new PluginException("金额不是数值: " + amount);
        }
    }

    /** 拉取源币种汇率表（缓存未命中时调用）。 */
    private Map<String, Double> fetchRates(String base, PluginContext context) throws PluginException {
        HttpResult response = context.http().get(API_URL + base);
        if (response == null || !response.isOk()) {
            throw new PluginException("汇率接口调用失败: "
                    + (response == null ? "null" : "HTTP " + response.getStatus()));
        }
        Map<String, Double> rates = new HashMap<>();
        Matcher matcher = RATE_PATTERN.matcher(response.getBody());
        while (matcher.find()) {
            rates.put(matcher.group(1), Double.valueOf(matcher.group(2)));
        }
        if (rates.isEmpty()) {
            throw new PluginException("汇率接口响应无法解析（响应列格式变更？）");
        }
        return rates;
    }

    /** 缓存 loader 抛受检异常的桥接载体（FetchFailure → get 后转回 PluginException）。 */
    private static final class FetchFailure extends RuntimeException {
        FetchFailure(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
