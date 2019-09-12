package com.pgmmers.radar.service.model;



import com.pgmmers.radar.dal.bean.AbstractionQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.model.AbstractionVO;

import java.util.List;

public interface AbstractionService {

     List<AbstractionVO> listAbstraction(Long modelId);

     AbstractionVO get(Long id);

     CommonResult list(Long modelId);

     CommonResult query(AbstractionQuery query);

     CommonResult save(AbstractionVO abstraction);

     CommonResult delete(Long[] id);

}
