package com.pgmmers.radar.service.engine;


import com.pgmmers.radar.enums.FieldType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 统计模块. 用于特征的提取。
 * @author feihu.wang
 *
 */
public interface AggregateCommand {

    /**
     * 
     * 累计.
     * 
     * @param modelId model id
     * @param searchField search field
     * @param searchFieldValue value of search field
     * @param refDateName ref time
     * @param begin begin time
     * @param end end time
     * @return qty {@link Long}
     * @author feihu.wang
     * 2016年8月4日
     */
    public long count(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin, Date end);
    
    /**
     * 
     * 累计去重.
     * 
     * @param modelId model id
     * @param searchField search field
     * @param searchFieldValue value of search field
     * @param refDateName ref time
     * @param begin begin time
     * @param end end time
     * @param distinctBy field of distinct
     * @return qty {@link Long}
     * @author feihu.wang
     * 2016年8月4日
     */
    public long distinctCount(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin, Date end,
                              String distinctBy);

    /**
     *
     * 求和.
     *
     * @param modelId model id
     * @param searchField search field
     * @param searchFieldValue value of search field
     * @param refDateName ref time
     * @param begin begin time
     * @param end end time
     * @param funcField field of sum
     * @return sum {@link BigDecimal}
     * @author feihu.wang
     * 2016年8月4日
     */
    public BigDecimal sum(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin, Date end,
                          String funcField);
    /**
     *
     * 平均.
     *
     * @param modelId model id
     * @param searchField search field
     * @param searchFieldValue value of search field
     * @param refDateName ref time
     * @param begin begin time
     * @param end end time
     * @param funcField field of average
     * @return a float num {@link BigDecimal}
     * @author feihu.wang
     * 2016年8月4日
     */
    public BigDecimal average(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin, Date end,
                              String funcField);

    /**
     *
     * 中位数.
     *
     * @param modelId model id
     * @param searchField search field
     * @param searchFieldValue value of search field
     * @param refDateName ref time
     * @param begin begin time
     * @param end end time
     * @param funcField field of median
     * @return a float mum {@link BigDecimal}
     * @author feihu.wang
     * 2016年8月4日
     */
    public BigDecimal median(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin, Date end,
                             String funcField);

    /**
     *
     * 最大.
     *
     * @param modelId model id
     * @param searchField search field
     * @param searchFieldValue value of search field
     * @param refDateName ref time
     * @param begin begin time
     * @param end end time
     * @param funcField field of median
     * @return a float mum {@link BigDecimal}
     * @author feihu.wang
     * 2016年8月4日
     */
    public BigDecimal max(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin, Date end,
                          String funcField);

    /**
     *
     * 最小.
     *
     * @param modelId model id
     * @param searchField search field
     * @param searchFieldValue value of search field
     * @param refDateName ref time
     * @param begin begin time
     * @param end end time
     * @param funcField field of median
     * @return a float mum {@link BigDecimal}
     * @author feihu.wang
     * 2016年8月4日
     */
    public BigDecimal min(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin, Date end,
                          String funcField);

    /**
     *
     * 标准差.
     *
     * @param modelId model id
     * @param searchField search field
     * @param searchFieldValue value of search field
     * @param refDateName ref time
     * @param begin begin time
     * @param end end time
     * @param funcField field of median
     * @param fieldType data type of funcField
     * @return a float mum {@link BigDecimal}
     * @author feihu.wang
     * 2016年8月4日
     */
    public BigDecimal sd(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin, Date end,
                         String funcField, FieldType fieldType);

    public BigDecimal variance(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin, Date end,
                               String funcField, FieldType fieldType);

    public BigDecimal deviation(String modelId, String searchField, Object searchFieldValue, String refDateName, Date begin, Date end,
                                String funcField, FieldType fieldType, BigDecimal sourceVal);
}
