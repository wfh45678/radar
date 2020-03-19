package com.pgmmers.radar.service.logs;


import com.pgmmers.radar.dal.bean.EventQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.TermQuery;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.List;

/**
 * 
 * @author wangfeihu
 *
 */
public interface EventService {

     List<Object> query(EventQuery query) throws IOException;
    
     PageResult<Object> query(TermQuery term);
    
     Workbook createExcel(List<Object> records, List<String> keyList4Field, List<String> titleList4Field, List<Object> keyList4Item, List<String> titleList4Item,
                                List<Object> keyList4Act, List<String> titleList4Act, List<Object> keyList4Rule, List<String> titleList4Rule);
    
}
