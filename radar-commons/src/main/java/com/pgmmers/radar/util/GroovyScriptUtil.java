package com.pgmmers.radar.util;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import java.util.HashMap;
import java.util.Map;

public class GroovyScriptUtil {

    public static Map<String, GroovyObject> passedClassMap = new HashMap<>();

    public static GroovyClassLoader groovyClassLoader;

    // 初始化GroovyClassLoader
    static {
        // TODO: 检查是否有更加合适的办法获取classLoader。目前这个方法绑定在Spring上。
        ClassLoader parent = AutowiredAnnotationBeanPostProcessor.class.getClassLoader();
        groovyClassLoader = new GroovyClassLoader(parent);
    }

    /**
     * 
     * 加载 groovy script.
     * 
     * @param script string of groovy script
     * @return {@link GroovyObject}
     * @author feihu.wang
     * 2016年8月2日
     */
    public static GroovyObject loadScript(String script) {
        GroovyObject groovyObject = passedClassMap.get(script.hashCode() + "");
        if (groovyObject == null) {
            Class groovyClass = groovyClassLoader.parseClass(script);
            try {
                groovyObject = (GroovyObject) groovyClass.newInstance();
                passedClassMap.put(script.hashCode() + "", groovyObject);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return groovyObject;
    }

    public static Object invokeMethod(GroovyObject object, String method, Object[] args) {
        return object.invokeMethod(method, args);
    }

    public static Object invokeMethod(String script, String method, Object[] args) {
        GroovyObject groovy = loadScript(script);
        if (groovy != null) {
            return invokeMethod(groovy, method, args);
        } else {
            return null;
        }
    }

    /**
     * 删除不在使用的脚本关联的groovy object, 不然内存有溢出风险。
     */
    public static void removeInactiveScript(String  script){
        passedClassMap.remove(script.hashCode() + "");
    }
}
