package com.pgmmers.radar.model.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 插件注册表持久化（v3.1）：插件生命周期管理的数据基础。
 * 启动时与注册表对账（upsert），启停/上传/删除即时更新。
 *
 * @since 3.1.0
 */
@TableName("engine_plugin_registry")
public class PluginRegistryPO {

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /** 插件 key（唯一）。 */
    @TableField("PLUGIN_KEY")
    private String pluginKey;

    /** 来源：BUILTIN_V2 / BUILTIN_V3 / EXTERNAL。 */
    @TableField("SOURCE")
    private String source;

    /** 外置插件 jar 文件名（内置插件为空）。 */
    @TableField("JAR_FILE")
    private String jarFile;

    /** 插件版本声明（@PluginMeta.version，可为空串）。 */
    @TableField("VERSION")
    private String version;

    /** 提供方声明（@PluginMeta.provider，可为空串）。 */
    @TableField("PROVIDER")
    private String provider;

    /** 1=启用 0=禁用（引擎执行链与 /common/plugins 双侧过滤）。 */
    @TableField("ENABLED")
    private Integer enabled;

    @TableField("CREATE_TIME")
    private Date createTime;

    @TableField("UPDATE_TIME")
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPluginKey() {
        return pluginKey;
    }

    public void setPluginKey(String pluginKey) {
        this.pluginKey = pluginKey;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getJarFile() {
        return jarFile;
    }

    public void setJarFile(String jarFile) {
        this.jarFile = jarFile;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
