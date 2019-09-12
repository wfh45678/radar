package com.pgmmers.radar.dal.data;

import java.util.List;

import com.pgmmers.radar.vo.data.MobileInfoVO;

public interface MobileInfoDal {
     List<MobileInfoVO> listMobileInfo(int pageNum, int pageSize);
    
     int countQty(MobileInfoVO vo);
    
     MobileInfoVO getMobileInfoByMobileNo(String mobile);
    
     List<MobileInfoVO> listMobileInfo(String province, String city, int pageNum, int pageSize);
    
     int save(MobileInfoVO info);

}
