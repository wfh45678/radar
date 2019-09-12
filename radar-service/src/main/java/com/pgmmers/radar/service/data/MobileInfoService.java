package com.pgmmers.radar.service.data;


import com.pgmmers.radar.vo.data.MobileInfoVO;

public interface MobileInfoService {

    /**
     * 
     * 手机号码归属地.
     * 
     * @param mobile 手机号码归属地
     * @return MobileInfoVO
     * @author feihu.wang
     * 2016年10月9日 feihu.wang
     */
     MobileInfoVO getMobileInfoByMobile(String mobile);
    
}
