package com.pgmmers.radar.service.model;



import com.pgmmers.radar.dal.bean.FieldQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.model.FieldVO;

import java.util.List;

public interface FieldService {

     List<FieldVO> listField(Long modelId);

     FieldVO get(Long id);

     CommonResult query(FieldQuery query);

     CommonResult save(FieldVO field);

     CommonResult delete(Long[] id);

}
