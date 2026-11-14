package com.pgmmers.radar.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.pgmmers.radar.common.util.GroovyScriptUtil;
import groovy.lang.GroovyObject;
import org.junit.jupiter.api.Test;

/**
 * L1 纯单测：Groovy 脚本加载/缓存/调用（Groovy 4 环境下的引擎核心行为）。
 */
class GroovyScriptUtilTest {

    private static final String ADD_SCRIPT =
            "class AddScript {\n"
            + "  int sum(int a, int b) {\n"
            + "    return a + b\n"
            + "  }\n"
            + "}\n";

    @Test
    void loadAndInvoke() {
        GroovyObject object = GroovyScriptUtil.loadScript(ADD_SCRIPT);
        assertNotNull(object);
        Object result = GroovyScriptUtil.invokeMethod(object, "sum", new Object[]{2, 3});
        assertEquals(5, result);
    }

    @Test
    void sameScriptSharesCachedInstance() {
        GroovyObject first = GroovyScriptUtil.loadScript(ADD_SCRIPT);
        GroovyObject second = GroovyScriptUtil.loadScript(ADD_SCRIPT);
        assertSame(first, second, "相同脚本应命中 passedClassMap 缓存返回同一实例");
    }

    @Test
    void removeInactiveScriptEvictsCache() {
        String script = ADD_SCRIPT.replace("AddScript", "EvictScript");
        GroovyObject object = GroovyScriptUtil.loadScript(script);
        assertNotNull(object);
        GroovyScriptUtil.removeInactiveScript(script);
        GroovyObject reloaded = GroovyScriptUtil.loadScript(script);
        assertNotNull(reloaded);
        assertSame(reloaded, GroovyScriptUtil.loadScript(script), "移除后重新加载并再次命中缓存");
    }

    @Test
    void syntaxErrorThrows() {
        // parseClass 的编译异常不在 loadScript 捕获范围内，属于约定行为
        assertThrows(Exception.class, () -> GroovyScriptUtil.loadScript("class Broken { def === }"));
    }

    @Test
    void nullScriptBehaviorLocked() {
        // 锁定现有行为：loadScript(null) 在 md5 计算处抛 NPE；
        // invokeMethod(GroovyObject, ...) 重载无 null 检查，直接 NPE（String script 重载才有 null 检查）。
        assertThrows(NullPointerException.class, () -> GroovyScriptUtil.loadScript(null));
        assertThrows(NullPointerException.class,
                () -> GroovyScriptUtil.invokeMethod((GroovyObject) null, "any", new Object[0]));
    }
}
