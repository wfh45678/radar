package com.pgmmers.radar.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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
 * L3 全链路集成测试：复刻 v2.0 冒烟回归场景——
 * 注册 → 登录(JWT) → 模板列表 → copy 模板 → build → enable → 事件上传 → 规则命中
 * → getScore → 命中报表。
 *
 * <p>验证码按冒烟回归同款方式绕过：直接向 Redis 写入
 * {@code log_captcha_<CODE>}（CacheService.validateCaptcha 只查该键存在性）。</p>
 *
 * <p>有序执行（@Order + PER_CLASS 保持登录态字段），各步骤断言失败即中止，输出携带接口返回原文便于定位。</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EngineFullChainTest extends AbstractIntegrationTest {

    private static final String CAPTCHA = "ITCAP";
    private static final String UNIQUE = String.valueOf(System.currentTimeMillis());
    private static final String LOGIN_NAME = "it_user_" + UNIQUE;
    private static final String PASSWORD = "it_pass_" + UNIQUE;
    private static final String MODEL_LABEL = "IT全链路_" + UNIQUE;
    private static final String REQ_ID = "it_req_" + UNIQUE;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private StringRedisTemplate redis;

    private String authToken;

    private String demoEventJson() {
        return "{\n"
                + "  \"eventId\":\"" + REQ_ID + "\", \"mobile\":\"18516249909\", \"userId\":\"" + LOGIN_NAME + "\",\n"
                + "  \"eventTime\":" + System.currentTimeMillis() + ", \"userIP\":\"180.175.166.148\",\n"
                + "  \"deviceId\": \"SDKSKDSLD-ASDFA-32348235\", \"os\":\"ios\", \"amount\":500.0,\n"
                + "  \"channel\":\"alipay\"\n"
                + "}";
    }

    private ResponseEntity<String> exchange(HttpMethod method, String url, HttpEntity<?> entity) {
        return rest.exchange(url, method, entity, String.class);
    }

    private HttpEntity<Object> withAuth(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-auth-token", authToken);
        if (body instanceof MultiValueMap) {
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return new HttpEntity<>(body, headers);
    }

    private JSONObject parse(ResponseEntity<String> response, String step) {
        assertNotNull(response.getBody(), step + " 无响应体");
        JSONObject json = JSONObject.parseObject(response.getBody());
        assertTrue(json.getBooleanValue("success"),
                step + " 失败: " + response.getBody());
        return json;
    }

    @Test
    @Order(1)
    void step1_registerAndLogin() {
        // 验证码直写 Redis（键格式见 CacheServiceImpl.LOGIN_CAPTCHA_PREFIX）
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
        assertNotNull(authToken, "登录应返回 x-auth-token");
    }

    private HttpHeaders formHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    @Test
    @Order(2)
    void step2_copyTemplateBuildAndEnable() {
        assertTrue(authToken != null, "依赖 step1 登录态");
        // 模板列表应包含种子模板 103
        JSONObject templates = parse(exchange(HttpMethod.GET, "/services/v1/model/list/template",
                withAuth(null)), "模板列表");
        JSONArray modelList = templates.getJSONObject("data").getJSONArray("modelList");
        assertTrue(modelList != null && !modelList.isEmpty(), "模板列表为空");
        long templateId = modelList.stream()
                .mapToLong(t -> ((JSONObject) t).getLongValue("id"))
                .filter(id -> id == 103L)
                .findFirst()
                .orElseThrow(() -> new AssertionError("种子模板 103 不存在: " + modelList));

        // copy
        JSONObject copyBody = new JSONObject();
        copyBody.put("id", templateId);
        copyBody.put("modelName", "IT链路模型_" + UNIQUE);
        copyBody.put("label", MODEL_LABEL);
        parse(exchange(HttpMethod.POST, "/services/v1/model/copy",
                withAuth(copyBody.toJSONString())), "copy 模板");

        // 从模型列表找到新模型（含 GUID）
        JSONObject list = parse(exchange(HttpMethod.GET, "/services/v1/model/list",
                withAuth(null)), "模型列表");
        JSONArray rows = list.getJSONObject("data").getJSONArray("modelList");
        if (rows == null) {
            rows = list.getJSONObject("data").getJSONArray("list");
        }
        assertNotNull(rows, "模型列表数据缺失: " + list);
        JSONObject copied = rows.stream()
                .map(r -> (JSONObject) r)
                .filter(r -> MODEL_LABEL.equals(r.getString("label")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("copy 后未找到模型: " + list));
        Long modelId = copied.getLong("id");
        String modelGuid = copied.getString("GUID");
        assertNotNull(modelId, "新模型 id 缺失");
        assertNotNull(modelGuid, "新模型 GUID 缺失");

        parse(exchange(HttpMethod.POST, "/services/v1/model/build/" + modelId,
                withAuth(null)), "build");
        parse(exchange(HttpMethod.POST, "/services/v1/model/enable/" + modelId,
                withAuth(null)), "enable");
    }

    @Test
    @Order(3)
    void step3_uploadEventAndGetScoreAndReport() {
        assertTrue(authToken != null, "依赖 step1 登录态");
        // 重新定位新模型（step2 的局部变量不跨方法）
        JSONObject list = parse(exchange(HttpMethod.GET, "/services/v1/model/list",
                withAuth(null)), "模型列表");
        JSONArray rows = list.getJSONObject("data").getJSONArray("modelList");
        if (rows == null) {
            rows = list.getJSONObject("data").getJSONArray("list");
        }
        JSONObject copied = rows.stream()
                .map(r -> (JSONObject) r)
                .filter(r -> MODEL_LABEL.equals(r.getString("label")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到 copy 的模型"));
        Long modelId = copied.getLong("id");
        String modelGuid = copied.getString("GUID");

        // 事件上传（公开接口，表单参数）
        MultiValueMap<String, String> uploadForm = new LinkedMultiValueMap<>();
        uploadForm.add("modelGuid", modelGuid);
        uploadForm.add("reqId", REQ_ID);
        uploadForm.add("jsonInfo", demoEventJson());
        JSONObject upload = parse(exchange(HttpMethod.POST, "/services/v1/uploadInfo",
                new HttpEntity<>(uploadForm, formHeaders())), "事件上传");
        assertTrue(upload.getJSONObject("data") != null
                        && upload.getJSONObject("data").containsKey("activations"),
                "上传结果应包含 activations 决策: " + upload);

        // getScore（Redis 通道）
        JSONObject score = parse(exchange(HttpMethod.GET,
                "/services/v1/getScore?modelGuid=" + modelGuid + "&reqId=" + REQ_ID,
                withAuth(null)), "getScore");
        assertNotNull(score.getJSONObject("data"), "getScore 数据缺失: " + score);

        // 规则命中报表
        String begin = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> query = new HashMap<>();
        query.put("modelId", modelId);
        query.put("beginTime", begin);
        query.put("endTime", end);
        parse(exchange(HttpMethod.POST, "/services/v1/rule/hitsSort/" + modelId,
                withAuth(new JSONObject(query).toJSONString())), "hitsSort 报表");

        // 事件检索（ES 通道，异步落库留缓冲，容忍一次空结果）
        Map<String, Object> search = new HashMap<>();
        search.put("modelId", modelId);
        search.put("beginTime", begin);
        search.put("endTime", end);
        JSONObject events = parse(exchange(HttpMethod.POST, "/services/v1/event/query",
                withAuth(new JSONObject(search).toJSONString())), "事件检索");
        JSONObject eventData = events.getJSONObject("data");
        assertFalse(eventData == null, "事件检索数据缺失");
    }
}
