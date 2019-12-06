package com.pgmmers.radar.service.model;

import com.pgmmers.radar.vo.model.MoldVO;

public interface MoldService {
    MoldVO get(Long id);
    MoldVO getByModelId(Long modelId);
}
