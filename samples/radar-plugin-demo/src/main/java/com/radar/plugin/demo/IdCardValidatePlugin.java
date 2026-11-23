package com.radar.plugin.demo;

import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.ParamOption;
import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.PluginContext;
import com.radar.plugin.api.PluginException;
import com.radar.plugin.api.PluginMeta;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 身份证校验插件（v3.5 官方示例）：18 位校验位算法 + 生日/性别/省份多列输出。
 *
 * <p>展示"纯算法"实现姿势：零依赖、零外呼，GB 11643-1999 校验位。
 * 15 位老号在宽松模式输出 legacy 标记不阻断，严格模式拒绝。
 * mode 参数演示 paramSchema 的 options 下拉。</p>
 */
@PluginMeta(key = "ID_CARD_VALIDATE", name = "身份证校验",
        desc = "18 位身份证校验位算法 + 提取生日/性别/省份（15 位老号宽松模式标记 legacy）",
        type = "JSON",
        version = "1.0.0", provider = "radar-official")
public class IdCardValidatePlugin implements Plugin {

    /** GB 11643-1999 校验位权重与码表。 */
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final String CHECK_CODES = "10X98765432";

    /** 省级行政区划码表（前 2 位 → 名称）。 */
    private static final Map<String, String> PROVINCES = buildProvinces();

    @Override
    public java.util.List<ParamDescriptor> paramSchema() {
        return java.util.List.of(
                ParamDescriptor.name("mode", "校验模式").required()
                        .options(ParamOption.of("宽松", "lenient"), ParamOption.of("严格", "strict"))
                        .defaultValue("lenient")
                        .placeholder("严格模式下校验失败抛异常（引擎隔离）").build());
    }

    @Override
    public java.util.List<OutputColumn> outputMeta() {
        return java.util.List.of(
                OutputColumn.of("valid", "校验结果", "STRING"),
                OutputColumn.of("birthday", "出生日期", "STRING"),
                OutputColumn.of("gender", "性别", "STRING"),
                OutputColumn.of("province", "省份", "STRING"));
    }

    @Override
    public PluginResult execute(PluginRequest request, PluginContext context) throws PluginException {
        Object raw = request.getSourceValues().isEmpty() ? null : request.getSourceValues().get(0);
        String idCard = raw == null ? "" : String.valueOf(raw).trim().toUpperCase();
        boolean strict = "strict".equalsIgnoreCase(
                String.valueOf(request.getArgs().getOrDefault("mode", "lenient")));

        Map<String, Object> out = new LinkedHashMap<>();
        if (idCard.isEmpty()) {
            out.put("valid", "false");
            return PluginResult.of(out);
        }

        if (idCard.length() == 15) {
            // 15 位老号：无校验位，补 19 世纪解析生日/性别
            if (strict) {
                throw new PluginException("15 位老身份证号，请升级为 18 位后重试: " + idCard);
            }
            out.put("valid", "legacy");
            out.put("birthday", "19" + idCard.substring(6, 8) + "-" + idCard.substring(8, 10)
                    + "-" + idCard.substring(10, 12));
            out.put("gender", Integer.parseInt(idCard.substring(14, 15)) % 2 == 0 ? "女" : "男");
            out.put("province", PROVINCES.getOrDefault(idCard.substring(0, 2), "未知"));
            return PluginResult.of(out);
        }

        boolean valid = idCard.length() == 18 && checksumValid(idCard) && birthValid(idCard);
        String birthday = "";
        String gender = "";
        String province = "未知";
        if (idCard.length() == 18) {
            try {
                birthday = LocalDate.parse(idCard.substring(6, 14).replaceFirst(
                        "(\\d{4})(\\d{2})(\\d{2})", "$1-$2-$3")).toString();
            } catch (DateTimeParseException e) {
                valid = false;
            }
            gender = Character.getNumericValue(idCard.charAt(16)) % 2 == 0 ? "女" : "男";
            province = PROVINCES.getOrDefault(idCard.substring(0, 2), "未知");
        }
        if (!valid && strict) {
            throw new PluginException("身份证校验失败: " + idCard);
        }
        out.put("valid", valid ? "true" : "false");
        out.put("birthday", birthday);
        out.put("gender", gender);
        out.put("province", province);
        return PluginResult.of(out);
    }

    /** GB 11643-1999 校验位。 */
    private boolean checksumValid(String idCard) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            int digit = Character.getNumericValue(idCard.charAt(i));
            if (digit < 0) {
                return false;
            }
            sum += digit * WEIGHTS[i];
        }
        return CHECK_CODES.charAt(sum % 11) == idCard.charAt(17);
    }

    /** 生日段必须可解析为真实日期。 */
    private boolean birthValid(String idCard) {
        try {
            LocalDate.parse(idCard.substring(6, 14).replaceFirst(
                    "(\\d{4})(\\d{2})(\\d{2})", "$1-$2-$3"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static Map<String, String> buildProvinces() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("11", "北京"); map.put("12", "天津"); map.put("13", "河北"); map.put("14", "山西");
        map.put("15", "内蒙古"); map.put("21", "辽宁"); map.put("22", "吉林"); map.put("23", "黑龙江");
        map.put("31", "上海"); map.put("32", "江苏"); map.put("33", "浙江"); map.put("34", "安徽");
        map.put("35", "福建"); map.put("36", "江西"); map.put("37", "山东"); map.put("41", "河南");
        map.put("42", "湖北"); map.put("43", "湖南"); map.put("44", "广东"); map.put("45", "广西");
        map.put("46", "海南"); map.put("50", "重庆"); map.put("51", "四川"); map.put("52", "贵州");
        map.put("53", "云南"); map.put("54", "西藏"); map.put("61", "陕西"); map.put("62", "甘肃");
        map.put("63", "青海"); map.put("64", "宁夏"); map.put("65", "新疆"); map.put("71", "台湾");
        map.put("81", "香港"); map.put("82", "澳门");
        return map;
    }
}
