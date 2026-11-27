package com.pgmmers.radar.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 官方模板质量红线（v3.4 T5，Testcontainers 真库全链路）：
 * 逐模板 HTTP 黑盒执行——模板导入（零 error 告警）→ 构建 → 启用 → 造事件 → 引擎出分。
 * 任何一个模板导入告警或构建失败，本测试即红——模板质量红线用例化后随 CI 常驻。
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
class TemplateRedLineTest extends AbstractIntegrationTest {

    private static final String CAPTCHA = "TPLCAP";
    private static final String UNIQUE = String.valueOf(System.currentTimeMillis()).substring(7);
    // label 须 <= 32 字符（engine_model.LABEL varchar(32)）：key 缩写 + 6 位时间戳
    private static final java.util.Map<String, String> KEY_ABBR = java.util.Map.of(
            "account-takeover", "ATO", "promo-abuse", "PRM", "transaction-risk", "TXN");
    private static final String LOGIN_NAME = "tpl_user_" + UNIQUE;
    private static final String PASSWORD = "tpl_pass_" + UNIQUE;

    private static final List<String> TEMPLATE_KEYS =
            List.of("account-takeover", "promo-abuse", "transaction-risk");

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private StringRedisTemplate redis;

    private String authToken;

    private ResponseEntity<String> exchange(HttpMethod method, String url, HttpEntity<?> entity) {
        return rest.exchange(url, method, entity, String.class);
    }

    private HttpEntity<Object> withAuth(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-auth-token", authToken);
        headers.setContentType(body instanceof MultiValueMap
                ? MediaType.APPLICATION_FORM_URLENCODED : MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private JSONObject parse(ResponseEntity<String> response, String step) {
        assertNotNull(response.getBody(), step + " 无响应体");
        JSONObject json = JSONObject.parseObject(response.getBody());
        assertTrue(json.getBooleanValue("success"), step + " 失败: " + response.getBody());
        return json;
    }

    @Test
    @Order(1)
    void step1_registerAndLogin() {
        redis.opsForValue().set("log_captcha_" + CAPTCHA, CAPTCHA, Duration.ofMinutes(2));

        MultiValueMap<String, String> registForm = new LinkedMultiValueMap<>();
        registForm.add("loginName", LOGIN_NAME);
        registForm.add("passwd", PASSWORD);
        registForm.add("verifyPasswd", PASSWORD);
        registForm.add("captcha", CAPTCHA);
        parse(exchange(HttpMethod.POST, "/services/v1/user/regist",
                new HttpEntity<>(registForm, formHeaders())), "注册");

        MultiValueMap<String, String> loginForm = new LinkedMultiValueMap<>();
        loginForm.add("loginName", LOGIN_NAME);
        loginForm.add("passwd", PASSWORD);
        loginForm.add("captcha", CAPTCHA);
        JSONObject login = parse(exchange(HttpMethod.POST, "/services/v1/user/login",
                new HttpEntity<>(loginForm, formHeaders())), "登录");
        authToken = login.getJSONObject("data").getString("x-auth-token");
        assertNotNull(authToken, "登录应下发 x-auth-token");
    }

    @Test
    @Order(2)
    void step2_eachTemplateImportsAndBuildsCleanly() {
        assertTrue(authToken != null, "依赖 step1 登录态");
        // 模板库列表：3 个官方内置模板可见
        JSONObject library = parse(exchange(HttpMethod.GET, "/services/v1/model/templates",
                withAuth(null)), "模板库");
        JSONArray templates = library.getJSONObject("data").getJSONArray("templates");
        assertNotNull(templates, "模板列表缺失");
        for (String key : TEMPLATE_KEYS) {
            assertTrue(templates.stream().anyMatch(t -> ((JSONObject) t).getString("key").equals(key)),
                    "内置模板应可见: " + key);
        }

        for (String key : TEMPLATE_KEYS) {
            // 一键创建（label 唯一：场景 + 时间戳）
            String label = "TPL红线_" + KEY_ABBR.get(key) + "_" + UNIQUE;
            ResponseEntity<String> resp = exchange(HttpMethod.POST,
                    "/services/v1/model/templates/" + key + "/import?label=" + label,
                    withAuth(null));
            JSONObject imported = parse(resp, "导入模板 " + key);
            Long modelId = imported.getJSONObject("data").getLong("id");
            assertNotNull(modelId, "导入应返回新模型 id");

            // 质量红线：导入告警必须为零（内置插件齐备 + Groovy 全部可编译）
            JSONArray warnings = imported.getJSONObject("data").getJSONArray("warnings");
            assertTrue(warnings == null || warnings.isEmpty(),
                    "模板 " + key + " 导入不应有告警: " + warnings);

            // 构建通过（Groovy/规则在引擎真实编译）
            parse(exchange(HttpMethod.POST, "/services/v1/model/build/" + modelId,
                    withAuth(null)), "构建 " + key);
            parse(exchange(HttpMethod.POST, "/services/v1/model/enable/" + modelId,
                    withAuth(null)), "启用 " + key);
        }
    }

    @Test
    @Order(3)
    void step3_eachTemplateAcceptsEventAndScores() {
        assertTrue(authToken != null, "依赖 step1 登录态");
        JSONObject list = parse(exchange(HttpMethod.GET, "/services/v1/model/list",
                withAuth(null)), "模型列表");
        JSONArray rows = list.getJSONObject("data").getJSONArray("modelList");
        if (rows == null) {
            rows = list.getJSONObject("data").getJSONArray("list");
        }
        assertNotNull(rows, "模型列表数据缺失");

        for (String key : TEMPLATE_KEYS) {
            String label = "TPL红线_" + KEY_ABBR.get(key) + "_" + UNIQUE;
            JSONObject model = rows.stream()
                    .map(r -> (JSONObject) r)
                    .filter(r -> label.equals(r.getString("label")))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("未找到模板模型: " + label));
            String modelGuid = model.getString("guid");
            assertNotNull(modelGuid, "模型 GUID 缺失");

            // 造事件（按模板字段构造：入口/实体字段必填，其余通用占位）
            String eventJson = eventJsonFor(key);
            MultiValueMap<String, String> uploadForm = new LinkedMultiValueMap<>();
            uploadForm.add("modelGuid", modelGuid);
            uploadForm.add("reqId", "tpl_req_" + key + "_" + UNIQUE);
            uploadForm.add("jsonInfo", eventJson);
            JSONObject upload = parse(exchange(HttpMethod.POST, "/services/v1/uploadInfo",
                    new HttpEntity<>(uploadForm, formHeaders())), "事件上传 " + key);
            // 引擎真实决策（红线：模板场景可被事件驱动并产出激活决策，断言口径同 EngineFullChainTest）
            assertTrue(upload.containsKey("strategies")
                    && !upload.getJSONObject("strategies").isEmpty(),
                    "上传结果应包含 strategies 决策: " + upload);
        }
    }

    /** 按场景构造事件：必填字段（登录/注册/交易三形态）+ 通用上下文。 */
    private String eventJsonFor(String key) {
        long now = System.currentTimeMillis();
        String timeField = switch (key) {
            case "account-takeover" -> "loginTime";
            case "promo-abuse" -> "registerTime";
            default -> "eventTime";
        };
        return "{"
                + "\"eventId\":\"tpl_evt_" + key + "_" + UNIQUE + "\","
                + "\"userId\":\"tpl_user_001\","
                + "\"" + timeField + "\":" + now + ","
                + "\"mobile\":\"18516249909\","
                + "\"userIP\":\"180.175.166.148\","
                + "\"deviceId\":\"TPL-DEVICE-" + key + "\","
                + "\"os\":\"ios\","
                + "\"amount\":500.0,"
                + "\"channel\":\"alipay\""
                + "}";
    }

    private HttpHeaders formHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }
}
