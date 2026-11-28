package com.pgmmers.radar.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pgmmers.radar.service.impl.model.transfer.ScenarioPackage;
import com.pgmmers.radar.service.impl.model.transfer.ScenarioPackageService;
import com.pgmmers.radar.service.impl.model.transfer.TemplateCatalogService;
import com.pgmmers.radar.service.impl.model.transfer.TemplateInfo;
import com.pgmmers.radar.service.impl.model.transfer.TemplateSourceService;
import com.pgmmers.radar.service.impl.model.transfer.ScenarioPackageService.ScenarioPackageException;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 模板库 L1 单测（v3.4）：内置 catalog 解析与统计、在线源 mock（合并/远程覆盖内置/
 * 降级/负缓存）、损坏包体拒绝。在线源用 JDK HttpServer 本地 mock，无外部依赖。
 */
class TemplateLibraryTest {

    private ScenarioPackageService packageService;
    private TemplateSourceService sourceService;
    private TemplateCatalogService catalogService;
    private HttpServer mockServer;
    private final AtomicInteger requestCount = new AtomicInteger();

    @BeforeEach
    void setUp() {
        packageService = new ScenarioPackageService();
        sourceService = new TemplateSourceService();
        catalogService = new TemplateCatalogService();
        ReflectionTestUtils.setField(packageService, "maxUploadSize", 10 * 1024 * 1024L);
        ReflectionTestUtils.setField(packageService, "recordWarnThreshold", 100000L);
        ReflectionTestUtils.setField(catalogService, "scenarioPackageService", packageService);
        ReflectionTestUtils.setField(catalogService, "templateSourceService", sourceService);
    }

    @AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.stop(0);
        }
    }

    private void startMockServer(String catalogBody, int respondStatus) throws Exception {
        mockServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockServer.createContext("/catalog.json", exchange -> {
            requestCount.incrementAndGet();
            byte[] body = catalogBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(respondStatus, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        mockServer.start();
        ReflectionTestUtils.setField(sourceService, "sourceUrl",
                "http://localhost:" + mockServer.getAddress().getPort() + "/catalog.json");
        ReflectionTestUtils.setField(sourceService, "timeoutMs", 3000L);
        ReflectionTestUtils.setField(sourceService, "negativeCacheMs", 60000L);
    }

    @Test
    void builtinCatalogLoadedWithStats() {
        catalogService.loadBuiltins();
        Map<String, Object> library = catalogService.library();
        @SuppressWarnings("unchecked")
        List<TemplateInfo> templates = (List<TemplateInfo>) library.get("templates");
        assertEquals(3, templates.size(), "三个官方内置模板");
        assertEquals("DISABLED", library.get("sourceStatus"), "未配置在线源=纯内置");

        TemplateInfo at = templates.stream()
                .filter(t -> t.getKey().equals("account-takeover")).findFirst().orElseThrow();
        assertEquals("账户盗用防护", at.getName());
        assertEquals(TemplateInfo.Source.BUILTIN, at.getSource());
        assertEquals(5, at.getStats().get("rules"));
        assertEquals(9, at.getStats().get("fields"));
    }

    @Test
    void remoteCatalogMergedAndOverridesBuiltin() throws Exception {
        // 远程 catalog：同名 key 覆盖内置 + 一个全新 key
        String remoteCatalog = """
                {"formatVersion":"1.0","templates":[
                  {"key":"account-takeover","file":"account-takeover.json","name":"账户盗用防护（在线修订版）"},
                  {"key":"brand-new","file":"brand-new.json","name":"全新在线模板"}
                ]}""";
        startMockServer(remoteCatalog, 200);
        catalogService.loadBuiltins();
        // 预热负缓存：首次真实拉取在 library() 内发生
        Map<String, Object> library = catalogService.library();
        @SuppressWarnings("unchecked")
        List<TemplateInfo> templates = (List<TemplateInfo>) library.get("templates");
        assertEquals("CONNECTED", library.get("sourceStatus"));

        // 远程同名 key 覆盖内置
        TemplateInfo at = templates.stream()
                .filter(t -> t.getKey().equals("account-takeover")).findFirst().orElseThrow();
        assertEquals(TemplateInfo.Source.REMOTE, at.getSource());
        assertEquals("账户盗用防护（在线修订版）", at.getName());
        // 全新 key 合入
        assertTrue(templates.stream().anyMatch(t -> t.getKey().equals("brand-new")),
                "在线新模板应合入列表");
        // 内置其余模板保留
        assertTrue(templates.stream().anyMatch(t -> t.getKey().equals("transaction-risk")
                && t.getSource() == TemplateInfo.Source.BUILTIN));
    }

    @Test
    void sourceFailureDegradesToBuiltinWithNegativeCache() throws Exception {
        startMockServer("internal error", 500);
        catalogService.loadBuiltins();

        Map<String, Object> first = catalogService.library();
        assertEquals("UNAVAILABLE", first.get("sourceStatus"), "拉取失败降级");
        int countAfterFirst = requestCount.get();

        Map<String, Object> second = catalogService.library();
        assertEquals("UNAVAILABLE", second.get("sourceStatus"));
        assertEquals(countAfterFirst, requestCount.get(), "负缓存窗口内不应重试");
        @SuppressWarnings("unchecked")
        List<TemplateInfo> templates = (List<TemplateInfo>) second.get("templates");
        assertEquals(3, templates.size(), "降级后仍展示内置模板");
    }

    @Test
    void disabledSourceBehavesAsPureBuiltin() {
        ReflectionTestUtils.setField(sourceService, "sourceUrl", "");
        catalogService.loadBuiltins();
        Map<String, Object> library = catalogService.library();
        assertEquals("DISABLED", library.get("sourceStatus"), "置空=禁用在线源");
        @SuppressWarnings("unchecked")
        List<TemplateInfo> templates = (List<TemplateInfo>) library.get("templates");
        assertEquals(3, templates.size());
        assertTrue(templates.stream().allMatch(t -> t.getSource() == TemplateInfo.Source.BUILTIN));
    }

    @Test
    void corruptRemoteBodyRejected() throws Exception {
        // catalog 合法但包体是垃圾 → parse 拒绝
        String remoteCatalog = """
                {"formatVersion":"1.0","templates":[
                  {"key":"bad","file":"bad.json","name":"坏包模板"}
                ]}""";
        startMockServer(remoteCatalog, 200);
        mockServer.createContext("/bad.json", exchange -> {
            byte[] body = "not a package".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        catalogService.loadBuiltins();
        // 坏包在 parse 层拒绝（真实调用链：bodyByKey 取回字节 → parse 校验）
        assertThrows(ScenarioPackageException.class,
                () -> packageService.parse(catalogService.bodyByKey("bad")));
        // 内置 key 不受影响
        byte[] body = catalogService.bodyByKey("account-takeover");
        assertTrue(body.length > 0);
        assertFalse(new String(body, StandardCharsets.UTF_8).isEmpty());
        ScenarioPackage ok = packageService.parse(body);
        assertEquals("账户盗用防护", ok.getScenario().getLabel());
    }
}
