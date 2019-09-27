package com.pgmmers.radar.vo.data;

import java.util.Date;

public class MobileInfoVO {

    private Long id;


    private String mobile;


    private String province;


    private String city;


    private String supplier;


    private String regionCode;


    private Date createTime;


    private Date updateTime;


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getMobile() {
        return mobile;
    }


    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }


    public String getProvince() {
        return province;
    }


    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }


    public String getCity() {
        return city;
    }


    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }


    public String getSupplier() {
        return supplier;
    }


    public void setSupplier(String supplier) {
        this.supplier = supplier == null ? null : supplier.trim();
    }


    public String getRegionCode() {
        return regionCode;
    }


    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode == null ? null : regionCode.trim();
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
