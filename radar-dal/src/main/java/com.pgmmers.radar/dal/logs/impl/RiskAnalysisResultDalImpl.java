package com.pgmmers.radar.dal.logs.impl;

import com.pgmmers.radar.dal.logs.RiskAnalysisResultDal;
import com.pgmmers.radar.vo.logs.RiskAnalysisResultVO;
import org.springframework.stereotype.Service;


@Service
public class RiskAnalysisResultDalImpl implements RiskAnalysisResultDal {


    
    @Override
    public int save(RiskAnalysisResultVO vo) {

        return 1;
    }

}
