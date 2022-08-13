package com.pgmmers.radar.dal.model;


import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.RuleHistoryQuery;
import com.pgmmers.radar.dal.bean.RuleQuery;
import com.pgmmers.radar.vo.model.RuleHistoryVO;
import com.pgmmers.radar.vo.model.RuleVO;

import java.util.List;


public interface RuleDal {

     RuleVO get(Long id);

     PageResult<RuleVO> query(RuleQuery query);

     int save(RuleVO model);

     int delete(Long[] id);
    
     PageResult<RuleHistoryVO> queryHistory(RuleHistoryQuery query);
    
     int saveHistory(RuleHistoryVO model);

     List<RuleVO> listByActId(Long actId);
}
