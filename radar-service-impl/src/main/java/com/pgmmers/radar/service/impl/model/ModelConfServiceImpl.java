package com.pgmmers.radar.service.impl.model;

import com.pgmmers.radar.dal.model.ModelConfDal;
import com.pgmmers.radar.service.model.ModelConfService;
import com.pgmmers.radar.vo.model.ModelConfVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ModelConfServiceImpl implements ModelConfService {
    @Resource
    private ModelConfDal modelConfDal;

    @Override
    public ModelConfVO get(Long id) {
        return modelConfDal.get(id);
    }

    @Override
    public ModelConfVO getByModelId(Long modelId) {
        return modelConfDal.getByModelId(modelId);
    }

    @Override
    public ModelConfVO save(ModelConfVO modelConf) {
        return modelConfDal.save(modelConf);
    }
}
