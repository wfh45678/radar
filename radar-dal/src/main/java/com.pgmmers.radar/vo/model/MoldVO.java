package com.pgmmers.radar.vo.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 机器学习模型配置，定义模型文件路径和参数
 * </p>
 *
 * @author guor
 * @date 2019/11/28
 */
public class MoldVO implements Serializable {
    /**
     * 自增ID，主键
     */
    private Long id;
    /**
     * 模型名称
     */
    private String name;
    /**
     * 输入层参数的key
     */
    private String feed;
    /**
     * 模型参数定义，需要注意参数顺序，改变顺序会使模型调用出错或者失效
     */
    private List<MoldParamVO> params;
    /**
     * 模型文件路径
     */
    private String path;
    /**
     * 模型更新时间
     */
    private Date updateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public List<MoldParamVO> getParams() {
        return params;
    }

    public void setParams(List<MoldParamVO> params) {
        this.params = params;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
