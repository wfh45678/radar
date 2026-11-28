package com.radar.plugin.api;

import java.util.Collections;
import java.util.List;

/**
 * Radar preItem 插件契约（v3）。
 *
 * <p>preItem 插件在事件进入规则运算前执行，把外部数据（归属地、数据服务结果等）
 * 以 {@code destField -> 结果} 的形式挂载进事件上下文，供 abstraction/rules 消费。</p>
 *
 * <p>实现要求：</p>
 * <ul>
 *   <li>实现类必须是 public 且有无参构造器（框架通过 SPI {@code META-INF/services} 或 Spring 容器发现）；</li>
 *   <li>类上标注 {@link FeatureMeta} 声明 key 等元信息；</li>
 *   <li>{@link #execute} 内不得自行管理长生命周期线程；远程调用请使用
 *       {@link FeatureContext#http()}（自带超时与私网防护），本地重复计算请使用
 *       {@link FeatureContext#cache(String, java.time.Duration)}；</li>
 *   <li>失败请抛 {@link FeatureException}，引擎默认隔离该次失败（事件链路继续），不影响其它插件。</li>
 * </ul>
 *
 * @since 3.0.0
 */
public interface FeatureProcessor {

    /**
     * 本插件的可配置参数描述（管理端据此动态渲染配置表单）。
     * 返回空列表表示插件无参数（管理端将回退为自由文本 args 输入）。
     */
    List<ParamDescriptor> paramSchema();

    /**
     * 输出列描述（当结果为多列结构时声明列名/标题/类型）。
     * 管理端据此自动生成响应列配置，无需手工填写 configJson。
     * 单值结果插件返回空列表即可。
     */
    default List<OutputColumn> outputMeta() {
        return Collections.emptyList();
    }

    /**
     * 执行插件。
     *
     * @param request 事件与配置的只读视图（源字段值、已按 schema 解析的参数等）
     * @param context 引擎提供的运行时工具（日志、缓存、HTTP）
     * @return 结果载荷；约定：单值直接返回，多列结构返回 {@code Map<String, Object>}
     * @throws FeatureException 业务失败（会被引擎隔离并计数，不影响事件链路）
     */
    FeatureResponse execute(FeatureRequest request, FeatureContext context) throws FeatureException;

    /** 插件 key（@FeatureMeta.key 优先，缺省为类简名）。 */
    default String key() {
        FeatureMeta meta = this.getClass().getAnnotation(FeatureMeta.class);
        if (meta != null && !meta.key().isEmpty()) {
            return meta.key();
        }
        return this.getClass().getSimpleName();
    }

    /** 展示名（@FeatureMeta.name 优先，缺省同 key）。 */
    default String name() {
        FeatureMeta meta = this.getClass().getAnnotation(FeatureMeta.class);
        return meta != null && !meta.name().isEmpty() ? meta.name() : key();
    }

    /** 功能描述（@FeatureMeta.desc）。 */
    default String desc() {
        FeatureMeta meta = this.getClass().getAnnotation(FeatureMeta.class);
        return meta != null ? meta.desc() : "";
    }

    /** 输出值类型标记（@FeatureMeta.type）。 */
    default String type() {
        FeatureMeta meta = this.getClass().getAnnotation(FeatureMeta.class);
        return meta != null ? meta.type() : "STRING";
    }

    /** 是否远程型（@FeatureMeta.remote）。 */
    default boolean remote() {
        FeatureMeta meta = this.getClass().getAnnotation(FeatureMeta.class);
        return meta != null && meta.remote();
    }

    /** 插件版本（@FeatureMeta.version，3.1.0 起提供，缺省空串）。 */
    default String version() {
        FeatureMeta meta = this.getClass().getAnnotation(FeatureMeta.class);
        return meta != null ? meta.version() : "";
    }

    /** 提供方（@FeatureMeta.provider，3.1.0 起提供，缺省空串）。 */
    default String provider() {
        FeatureMeta meta = this.getClass().getAnnotation(FeatureMeta.class);
        return meta != null ? meta.provider() : "";
    }
}
