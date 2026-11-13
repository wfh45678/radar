package com.pgmmers.radar.service.model;

import com.pgmmers.radar.model.vo.model.ModelConfParamVO;

public interface ModelConfParamService {

    ModelConfParamVO get(Long id);

    ModelConfParamVO save(ModelConfParamVO modelConfParam);

}
