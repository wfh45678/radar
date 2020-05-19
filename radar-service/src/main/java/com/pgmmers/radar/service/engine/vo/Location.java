package com.pgmmers.radar.service.engine.vo;

import java.util.StringJoiner;

public class Location {

    private String country = "中国";

    private String region = "";

    private String province = "";

    private String city = "";

    private String address = "";

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", Location.class.getSimpleName() + "[", "]")
                .add("country='" + country + "'")
                .add("region='" + region + "'")
                .add("province='" + province + "'")
                .add("city='" + city + "'")
                .add("address='" + address + "'")
                .toString();
    }
}
