package com.pgmmers.radar.service.model;



import com.pgmmers.radar.model.query.FieldQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.model.vo.model.FieldVO;

import java.util.List;

public interface FieldService {

     List<FieldVO> listField(Long modelId);

     FieldVO get(Long id);

     CommonResult query(FieldQuery query);

     CommonResult save(FieldVO field);

     CommonResult delete(Long[] id);

}
