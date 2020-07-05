package com.pgmmers.radar.service.model;



import com.pgmmers.radar.dal.bean.ModelQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.model.ModelVO;

import java.io.IOException;
import java.util.List;

public interface ModelService {

	 List<ModelVO> listModel(String merchantCode, Integer status);

     List<ModelVO> listModel(Integer status);

     List<ModelVO> listTemplateModel(boolean isTemplate);

     ModelVO getModelByGuid(String guid);

     ModelVO getModelById(Long id);

     CommonResult query(ModelQuery query);

     CommonResult save(ModelVO model);

     CommonResult delete(Long[] id);

     CommonResult build(Long id) throws IOException;

     CommonResult copy(Long id, String merchantCode, String name, String label);

}
