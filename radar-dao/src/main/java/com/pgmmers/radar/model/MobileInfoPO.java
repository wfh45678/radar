package com.pgmmers.radar.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "data_moble_info")
public class MobileInfoPO {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 主键
     */
    @Column(name = "MOBILE")
    private String mobile;

    /**
     * 省
     */
    @Column(name = "PROVINCE")
    private String province;

    /**
     * 市
     */
    @Column(name = "CITY")
    private String city;

    /**
     * 卡信息
     */
    @Column(name = "SUPPLIER")
    private String supplier;

    /**
     * 区号
     */
    @Column(name = "REGION_CODE")
    private String regionCode;

    @Column(name = "CREATE_TIME")
    private Date createTime;

    @Column(name = "UPDATE_TIME")
    private Date updateTime;

    /**
     * @return ID
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取主键
     *
     * @return MOBILE - 主键
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * 设置主键
     *
     * @param mobile 主键
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    /**
     * 获取省
     *
     * @return PROVINCE - 省
     */
    public String getProvince() {
        return province;
    }

    /**
     * 设置省
     *
     * @param province 省
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * 获取市
     *
     * @return CITY - 市
     */
    public String getCity() {
        return city;
    }

    /**
     * 设置市
     *
     * @param city 市
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获取卡信息
     *
     * @return SUPPLIER - 卡信息
     */
    public String getSupplier() {
        return supplier;
    }

    /**
     * 设置卡信息
     *
     * @param supplier 卡信息
     */
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    /**
     * 获取区号
     *
     * @return REGION_CODE - 区号
     */
    public String getRegionCode() {
        return regionCode;
    }

    /**
     * 设置区号
     *
     * @param regionCode 区号
     */
    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    /**
     * @return CREATE_TIME
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return UPDATE_TIME
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}