package com.pgmmers.radar.service.impl.model;

import com.pgmmers.radar.dal.model.MoldDal;
import com.pgmmers.radar.service.model.MoldService;
import com.pgmmers.radar.vo.model.MoldVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MoldServiceImpl implements MoldService {
    @Resource
    private MoldDal moldDal;

    @Override
    public MoldVO get(Long id) {
        return moldDal.get(id);
    }
}
