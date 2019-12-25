package com.pgmmers.radar.dal.model;

import com.pgmmers.radar.vo.model.ModelConfVO;

public interface ModelConfDal {
    ModelConfVO get(Long id);

    ModelConfVO getByModelId(Long modelId);
}
