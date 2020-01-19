package com.pgmmers.radar.service.impl.model;

import com.pgmmers.radar.dal.model.ModelConfDal;
import com.pgmmers.radar.service.model.ModelConfParamService;
import com.pgmmers.radar.vo.model.ModelConfParamVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModelConfParamServiceImpl implements ModelConfParamService {

    @Autowired
    private ModelConfDal modelConfDal;


    @Override
    public ModelConfParamVO get(Long id) {
        ModelConfParamVO paramVO;
        paramVO = modelConfDal.getParamById(id);
        return paramVO;
    }

    @Override
    public ModelConfParamVO save(ModelConfParamVO modelConfParam) {
        return modelConfDal.saveParam(modelConfParam);
    }
}
