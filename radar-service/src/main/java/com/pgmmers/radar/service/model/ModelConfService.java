package com.pgmmers.radar.service.model;

import com.pgmmers.radar.vo.model.ModelConfVO;

public interface ModelConfService {
    ModelConfVO get(Long id);

    ModelConfVO getByModelId(Long modelId);

    ModelConfVO save(ModelConfVO modelConf);

}
