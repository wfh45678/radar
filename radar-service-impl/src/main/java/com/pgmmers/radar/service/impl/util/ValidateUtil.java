package com.pgmmers.radar.service.impl.util;

import org.springframework.util.StringUtils;

public class ValidateUtil {
    //^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$
    /** Constant <code>EMAIL_REG="^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@("{trunked}</code> */
    public static final String EMAIL_REG = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)*\\.)+[a-zA-Z]{2,}$";

    /**
     *
     * 域名检查.
     *
     * @param url a {@link String} object.
     * @author feihu.wang
     * @since 2015年12月22日
     * @return a boolean.
     */
    public static boolean checkWebDomain(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        } else {
            boolean matches = url.matches("www.(\\w|-){2,}.(\\w|.){2,25}[a-z]$");
            return matches;
        }
    }

    /**
     *
     * 检查手机号码.
     *
     * @param mobile a {@link String} object.
     * @author feihu.wang
     * @since 2015年12月22日
     * @return a boolean.
     */
    public static boolean checkMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return false;
        } else {
            boolean matches = mobile.matches("1(3|4|5|7|8){1}([0-9]){9}");
            return matches;
        }
    }

    /**
     *
     * 检查邮件.
     *
     * @param email a {@link String} object.
     * @author feihu.wang
     * @since 2015年12月23日
     * @return a boolean.
     */
    public static boolean checkEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        } else {
            boolean matches = email.matches(EMAIL_REG);
            return matches;
        }
    }

    public static boolean checkIp(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        } else {
            boolean matches = ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
            return matches;
        }
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link String} objects.
     */
    public static void main(String[] args) {
        System.out.println(checkWebDomain("www.xwf-id.com"));
        System.out.println(checkMobile("18516249999"));
        System.out.println(checkEmail("feihu.wang@xwf-id.com"));
        System.out.println(checkIp("172.0.0.1"));
    }
}
