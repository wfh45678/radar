package com.radar.plugin.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 插件元信息声明。推荐所有 v3 插件显式标注；未标注时元信息回退为类简名/空串。
 *
 * <p>plugin key 是管理端 preItem 配置引用插件的唯一标识（历史遗留为类简名，如 IP2LOCATION）。
 * v3 起 key 与类名解耦，由本注解显式声明。</p>
 *
 * @since 3.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated
public @interface PluginMeta {

    /** 插件唯一标识（大写字母/数字/下划线，同库内不得重复）。 */
    String key();

    /** 展示名（管理端下拉可见），缺省同 key。 */
    String name() default "";

    /** 功能描述。 */
    String desc() default "";

    /** 输出值类型：STRING / JSON 等自由标记，供管理端展示。 */
    String type() default "STRING";

    /**
     * 是否远程型插件（执行时会发起外部网络调用）。
     * 远程型插件受引擎 HTTP 工具的强制超时与私网访问防护约束。
     */
    boolean remote() default false;

    /**
     * 插件版本（管理端展示用，语义随插件作者自定，建议 semver）。
     *
     * @since 3.1.0
     */
    String version() default "";

    /**
     * 提供方（作者/厂商名，管理端展示用）。
     *
     * @since 3.1.0
     */
    String provider() default "";
}
