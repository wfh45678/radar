package com.pgmmers.radar.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        String modelGuid = copied.getString("guid");
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
        String modelGuid = copied.getString("guid");

        // 事件上传（公开接口，表单参数；两条——频次类规则需第二笔才达阈值，同 v2.0 冒烟口径）
        MultiValueMap<String, String> uploadForm = new LinkedMultiValueMap<>();
        uploadForm.add("modelGuid", modelGuid);
        uploadForm.add("reqId", REQ_ID);
        uploadForm.add("jsonInfo", demoEventJson());
        JSONObject upload = parse(exchange(HttpMethod.POST, "/services/v1/uploadInfo",
                new HttpEntity<>(uploadForm, formHeaders())), "事件上传1");

        MultiValueMap<String, String> uploadForm2 = new LinkedMultiValueMap<>();
        uploadForm2.add("modelGuid", modelGuid);
        uploadForm2.add("reqId", REQ_ID + "_b");
        uploadForm2.add("jsonInfo", demoEventJson().replace("\"eventId\":\"" + REQ_ID + "\"",
                "\"eventId\":\"" + REQ_ID + "_b\""));
        JSONObject upload2 = parse(exchange(HttpMethod.POST, "/services/v1/uploadInfo",
                new HttpEntity<>(uploadForm2, formHeaders())), "事件上传2");
        // activations 键为引擎对外契约（v4.0 策略更名后保持不变）
        assertTrue(upload2.containsKey("activations")
                        && !upload2.getJSONObject("activations").isEmpty(),
                "上传结果应包含 activations 决策: " + upload2);

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

    /**
     * v4.0 策略类型全链路：导入 transaction-risk 官方模板（含 规则集+评分卡+决策树 三种类型）
     * → build → enable → 事件上传 → 断言三种策略均产出决策与命中明细
     * （评分卡 hitsDetail 为 feature_* 特征分箱，决策树为 node_* 访问路径）。
     */
    @Test
    @Order(4)
    void step4_strategyTypesEndToEnd() {
        assertTrue(authToken != null, "依赖 step1 登录态");
        // 模板库应含 transaction-risk（v4.0 起该模板覆盖三种策略类型）
        JSONObject library = parse(exchange(HttpMethod.GET, "/services/v1/model/templates",
                withAuth(null)), "模板库");
        JSONArray templateList = library.getJSONObject("data").getJSONArray("list");
        if (templateList == null) {
            templateList = library.getJSONObject("data").getJSONArray("templates");
        }
        assertNotNull(templateList, "模板库列表缺失: " + library);
        assertTrue(templateList.stream().map(t -> (JSONObject) t)
                        .anyMatch(t -> "transaction-risk".equals(t.getString("key"))),
                "模板库缺少 transaction-risk: " + templateList);

        // 导入（label/modelName 仅用于本次回查定位，label 唯一）
        JSONObject imported = parse(exchange(HttpMethod.POST,
                "/services/v1/model/templates/transaction-risk/import?modelName=it_strategy_types_" + UNIQUE,
                withAuth(null)), "模板导入");
        Long treeModelId = imported.getJSONObject("data").getLong("id");
        assertNotNull(treeModelId, "导入模板应返回新模型 id: " + imported);

        parse(exchange(HttpMethod.POST, "/services/v1/model/build/" + treeModelId,
                withAuth(null)), "策略类型模板 build");
        parse(exchange(HttpMethod.POST, "/services/v1/model/enable/" + treeModelId,
                withAuth(null)), "策略类型模板 enable");

        // 取 GUID 上传事件
        JSONObject modelDetail = parse(exchange(HttpMethod.GET, "/services/v1/model/" + treeModelId,
                withAuth(null)), "模板模型详情");
        String treeGuid = modelDetail.getJSONObject("data").getJSONObject("model").getString("guid");
        assertNotNull(treeGuid, "模板模型 GUID 缺失");

        String reqId = "it_types_" + UNIQUE;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("modelGuid", treeGuid);
        form.add("reqId", reqId);
        form.add("jsonInfo", demoEventJson().replace("\"eventId\":\"" + REQ_ID + "\"", "\"eventId\":\"" + reqId + "\""));
        JSONObject upload = parse(exchange(HttpMethod.POST, "/services/v1/uploadInfo",
                new HttpEntity<>(form, formHeaders())), "策略类型事件上传");

        // 三种策略类型都应产出决策（键为策略名）
        JSONObject activations = upload.getJSONObject("activations");
        assertNotNull(activations, "决策结果缺失: " + upload);
        assertTrue(activations.containsKey("transaction_exception"), "规则集策略未产出决策: " + activations);
        assertTrue(activations.containsKey("transaction_scorecard"), "评分卡策略未产出决策: " + activations);
        assertTrue(activations.containsKey("transaction_tree"), "决策树策略未产出决策: " + activations);

        // 评分卡：特征分箱明细（feature_*）；首笔事件（频次=1、金额=500）总分 480 → pass
        assertEquals("pass", activations.getJSONObject("transaction_scorecard").getString("risk"),
                "评分卡决策与预期不符: " + activations);
        JSONArray scorecardHits = upload.getJSONObject("hitsDetail").getJSONArray("transaction_scorecard");
        assertNotNull(scorecardHits, "评分卡命中明细缺失");
        assertTrue(scorecardHits.stream().map(h -> ((JSONObject) h).getString("key"))
                        .allMatch(k -> k.startsWith("feature_")),
                "评分卡明细 key 应为特征标识: " + scorecardHits);

        // 决策树：路径明细（node_*）；白名单为空必走 N 分支，后续分支依赖 IP 插件可用性，容忍 pass/reject
        String treeRisk = activations.getJSONObject("transaction_tree").getString("risk");
        assertTrue("pass".equals(treeRisk) || "reject".equals(treeRisk),
                "决策树决策异常: " + activations);
        JSONArray treeHits = upload.getJSONObject("hitsDetail").getJSONArray("transaction_tree");
        assertNotNull(treeHits, "决策树命中明细缺失");
        assertTrue(treeHits.stream().map(h -> ((JSONObject) h).getString("key"))
                        .anyMatch(k -> "node_n1".equals(k)),
                "决策树明细应含根节点路径: " + treeHits);
    }
}
