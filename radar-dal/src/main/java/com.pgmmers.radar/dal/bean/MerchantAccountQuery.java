package com.pgmmers.radar.dal.bean;

public class MerchantAccountQuery extends PageQuery {

    private String loginName;

    private String passwd;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

}
