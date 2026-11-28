package com.radar.plugin.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.radar.plugin.api.FeatureProcessor;
import com.radar.plugin.api.FeatureException;
import com.radar.plugin.api.FeatureResponse;
import com.radar.plugin.testkit.MockHttpTool;
import com.radar.plugin.testkit.FeatureRequestBuilder;
import com.radar.plugin.testkit.TestContexts;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * v3.5 种子插件三件套单测：ID_CARD_VALIDATE（纯算法）/ USER_AGENT_PARSE（正则规则表）/
 * BANK_CARD_CHECK（前缀映射表）。测试数据全部为 GB 标准样例号/虚构卡号，无真实数据。
 */
class SeedPluginsTest {

    @SuppressWarnings("unchecked")
    private Map<String, Object> execute(FeatureProcessor processor, Object sourceValue, Map<String, Object> args)
            throws FeatureException {
        FeatureResponse result = processor.execute(
                FeatureRequestBuilder.create()
                        .sourceField("input", sourceValue)
                        .args(args)
                        .build(),
                TestContexts.context(new MockHttpTool()));
        return (Map<String, Object>) result.getPayload();
    }

    // ---------- ID_CARD_VALIDATE ----------

    @Test
    void idCardValidOutput() throws FeatureException {
        Map<String, Object> out = execute(new IdCardValidatePlugin(), "110101199003077168", Map.of());
        assertEquals("true", out.get("valid"));
        assertEquals("1990-03-07", out.get("birthday"));
        assertEquals("女", out.get("gender"));
        assertEquals("北京", out.get("province"));
    }

    @Test
    void idCardGenderAndProvince() throws FeatureException {
        Map<String, Object> sh = execute(new IdCardValidatePlugin(), "310101199001011621", Map.of());
        assertEquals("女", sh.get("gender"));
        assertEquals("上海", sh.get("province"));
        Map<String, Object> gd = execute(new IdCardValidatePlugin(), "440301198511112137", Map.of());
        assertEquals("广东", gd.get("province"));
    }

    @Test
    void idCardChecksumFailureMarksInvalid() throws FeatureException {
        Map<String, Object> out = execute(new IdCardValidatePlugin(), "110101199003077160", Map.of());
        assertEquals("false", out.get("valid"));
    }

    @Test
    void idCardStrictModeThrows() {
        FeatureException e = assertThrows(FeatureException.class,
                () -> execute(new IdCardValidatePlugin(), "110101199003077160", Map.of("mode", "strict")));
        assertTrue(e.getMessage().contains("身份证校验失败"));
    }

    @Test
    void idCardLegacy15DigitInLenientMode() throws FeatureException {
        Map<String, Object> out = execute(new IdCardValidatePlugin(), "110101491001123", Map.of());
        assertEquals("legacy", out.get("valid"));
        assertEquals("1949-10-01", out.get("birthday"));
        assertEquals("男", out.get("gender"));
    }

    @Test
    void idCardLegacy15DigitRejectedInStrictMode() {
        assertThrows(FeatureException.class,
                () -> execute(new IdCardValidatePlugin(), "110101491001123", Map.of("mode", "strict")));
    }

    @Test
    void idCardMetadataDeclared() {
        FeatureProcessor processor = new IdCardValidatePlugin();
        assertEquals("ID_CARD_VALIDATE", processor.key());
        assertFalse(processor.remote());
        assertEquals(1, processor.paramSchema().size());
        assertEquals(4, processor.outputMeta().size());
    }

    // ---------- USER_AGENT_PARSE ----------

    @Test
    void uaWechatMobile() throws FeatureException {
        Map<String, Object> out = execute(new UserAgentParsePlugin(),
                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 "
                        + "(KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.42",
                Map.of());
        assertEquals("mobile", out.get("deviceType"));
        assertEquals("iOS", out.get("os"));
        assertEquals("微信内置", out.get("browser"));
        assertEquals("true", out.get("inWechat"));
        assertEquals("false", out.get("inAlipay"));
    }

    @Test
    void uaAndroidPcChrome() throws FeatureException {
        Map<String, Object> out = execute(new UserAgentParsePlugin(),
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                Map.of());
        assertEquals("pc", out.get("deviceType"));
        assertEquals("Windows", out.get("os"));
        assertEquals("Chrome", out.get("browser"));
        assertEquals("false", out.get("inWechat"));
    }

    @Test
    void uaAndroidTabletWithoutMobile() throws FeatureException {
        Map<String, Object> out = execute(new UserAgentParsePlugin(),
                "Mozilla/5.0 (Linux; Android 13; SM-X710) AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Chrome/120.0.0.0 Safari/537.36",
                Map.of());
        assertEquals("tablet", out.get("deviceType"));
        assertEquals("Android", out.get("os"));
    }

    @Test
    void uaEmptyGivesUnknown() throws FeatureException {
        Map<String, Object> out = execute(new UserAgentParsePlugin(), "", Map.of());
        assertEquals("unknown", out.get("deviceType"));
        assertEquals("其他", out.get("os"));
        assertEquals("其他", out.get("browser"));
    }

    // ---------- BANK_CARD_CHECK ----------

    @Test
    void bankCardValidDebitWithBank() throws FeatureException {
        Map<String, Object> out = execute(new BankCardCheckPlugin(), "6222021234567894", Map.of());
        assertEquals("true", out.get("valid"));
        assertEquals("借记", out.get("cardType"));
        assertEquals("建设银行", out.get("bank"));
    }

    @Test
    void bankCardValidVisaCredit() throws FeatureException {
        Map<String, Object> out = execute(new BankCardCheckPlugin(), "4111111111111111", Map.of());
        assertEquals("true", out.get("valid"));
        assertEquals("贷记", out.get("cardType"));
        // 4111 未入示例前缀表 → 宁缺毋错
        assertEquals("", out.get("bank"));
    }

    @Test
    void bankCardLuhnFailureMarksInvalid() throws FeatureException {
        Map<String, Object> out = execute(new BankCardCheckPlugin(), "6222021234567895", Map.of());
        assertEquals("false", out.get("valid"));
    }

    @Test
    void bankCardSpacesAreStripped() throws FeatureException {
        Map<String, Object> out = execute(new BankCardCheckPlugin(), "6222 0212 3456 7894", Map.of());
        assertEquals("true", out.get("valid"));
    }

    // ---------- 公共 ----------

    @Test
    void threePluginsRegisteredKeys() {
        assertEquals("USER_AGENT_PARSE", new UserAgentParsePlugin().key());
        assertEquals("BANK_CARD_CHECK", new BankCardCheckPlugin().key());
        Assertions.assertFalse(new BankCardCheckPlugin().remote());
        Assertions.assertFalse(new UserAgentParsePlugin().remote());
    }
}
