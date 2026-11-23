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
 * 银行卡校验插件（v3.5 官方示例）：Luhn 校验 + BIN 卡种粗判 + 主流银行前缀表。
 *
 * <p>展示"前缀映射表"实现姿势：Luhn 模 10 算法 + 精简 BIN 前缀表（示例性质，非完整/非权威——
 * 精确卡 BIN 库请参考 PLUGIN_REGISTRY 社区插件）。卡种粗判：16 位 4/5 开头默认贷记，
 * 6 开头默认借记，其余未知。</p>
 */
@PluginMeta(key = "BANK_CARD_CHECK", name = "银行卡校验",
        desc = "Luhn 模 10 校验 + 卡种粗判（借记/贷记）+ 主流银行前缀识别（示例前缀表）",
        type = "JSON",
        version = "1.0.0", provider = "radar-official")
public class BankCardCheckPlugin implements Plugin {

    /** 示例 BIN 前缀表（前 4 位 → 银行名）：仅覆盖主流卡段，非完整/非权威。 */
    private static final Map<String, String> BIN_BANKS = buildBins();

    @Override
    public java.util.List<ParamDescriptor> paramSchema() {
        return java.util.List.of();
    }

    @Override
    public java.util.List<OutputColumn> outputMeta() {
        return java.util.List.of(
                OutputColumn.of("valid", "Luhn 校验", "STRING"),
                OutputColumn.of("cardType", "卡种", "STRING"),
                OutputColumn.of("bank", "发卡银行", "STRING"));
    }

    @Override
    public PluginResult execute(PluginRequest request, PluginContext context) throws PluginException {
        Object raw = request.getSourceValues().isEmpty() ? null : request.getSourceValues().get(0);
        String card = raw == null ? "" : String.valueOf(raw).replaceAll("[\\s-]", "");

        Map<String, Object> out = new LinkedHashMap<>();
        boolean valid = !card.isEmpty() && card.matches("\\d{13,19}") && luhn(card);
        out.put("valid", valid ? "true" : "false");
        out.put("cardType", cardType(card));
        out.put("bank", bank(card));
        return PluginResult.of(out);
    }

    /** Luhn 模 10 算法。 */
    private boolean luhn(String card) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = card.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(card.charAt(i));
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }

    /** 卡种粗判：16 位 4/5 开头默认贷记，6 开头默认借记，其余未知。 */
    private String cardType(String card) {
        if (!card.matches("\\d{13,19}")) {
            return "未知";
        }
        if (card.startsWith("4") || card.matches("5[1-5].*")) {
            return "贷记";
        }
        if (card.startsWith("6")) {
            return "借记";
        }
        if (card.matches("3[47].*")) {
            return "贷记";
        }
        return "未知";
    }

    /** 银行识别：前 4 位前缀表精确匹配，未命中返回空串（宁缺毋错）。 */
    private String bank(String card) {
        if (card.length() < 4) {
            return "";
        }
        return BIN_BANKS.getOrDefault(card.substring(0, 4), "");
    }

    private static Map<String, String> buildBins() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("6217", "建设银行");
        map.put("6216", "建设银行");
        map.put("6227", "建设银行");
        map.put("6222", "建设银行");
        map.put("6225", "招商银行");
        map.put("6226", "中国银行");
        map.put("6228", "农业银行");
        map.put("6221", "工商银行");
        map.put("6205", "工商银行");
        map.put("6033", "交通银行");
        map.put("6223", "交通银行");
        map.put("6203", "邮政储蓄银行");
        map.put("6210", "邮政储蓄银行");
        map.put("6211", "邮政储蓄银行");
        map.put("6215", "浦发银行");
        map.put("6034", "中信银行");
        return map;
    }
}
