package com.pgmmers.radar.vo.model;

import java.io.Serializable;

/**
 * <p>
 * 机器学习模型配置，目前只考虑输入层为离散值的情况，不考虑需要词嵌入和融入卷积层，其中
 * 离散值通过表达式取数从前置流程传递过来.
 * </p>
 *
 * @author guor
 * @date 2019/11/28
 */
public class ModelConfParamVO implements Serializable {
    private Long id;
    /**
     * 参数的key
     */
    private String feed;
    /**
     * 取数表达式，英文逗号分隔fields.deviceId，abstractions.log_uid_ip_1_day_qty
     */
    private String expressions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public String getExpressions() {
        return expressions;
    }

    public void setExpressions(String expressions) {
        this.expressions = expressions;
    }

    @Override
    public String toString() {
        return "ModelConfParamVO{" +
                "id=" + id +
                ", feed='" + feed + '\'' +
                ", expressions='" + expressions + '\'' +
                '}';
    }
}
