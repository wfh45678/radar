package com.pgmmers.radar.service.engine;

import com.pgmmers.radar.service.engine.vo.Location;

import java.util.List;

public interface PluginService {

     Location ip2location(String ip);
    
     Location gps2location(String lng, String lat);
    
    /**
     * 
     * 多字段合并.
     * 
     * @param fields fields
     * @param combineType {@link com.pgmmers.radar.enums.CombineType}
     * @return object
     * @author feihu.wang
     * 2016年8月19日
     */
     Object allInOne(List<Object> fields, int combineType);

    /**
     * 
     * 字符串截短.
     * 
     * @param field field
     * @param start start
     * @param end end
     * @author feihu.wang
     * 2016年8月19日
     */
     String subString(String field, Integer start, Integer end);
    
    /**
     * 
     * 手机号码归属地.
     * 
     * @param mobile mobile as string
     * @return location of mobile
     * @author feihu.wang
     * 2016年8月20日
     */
     Location mobile2location(String mobile);
    
     String getSensitiveTime(Long timemills);

     String formatDate(Long timemills, String format);
}
