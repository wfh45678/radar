package com.pgmmers.radar.service.model;



import com.pgmmers.radar.model.query.ScenarioQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.model.vo.model.ScenarioVO;

import java.io.IOException;
import java.util.List;

public interface ScenarioService {

	 List<ScenarioVO> listScenario(String merchantCode, Integer status);

     List<ScenarioVO> listScenario(Integer status);

     List<ScenarioVO> listTemplateScenario(boolean isTemplate);

     ScenarioVO getModelByGuid(String guid);

     ScenarioVO getScenarioById(Long id);

     CommonResult query(ScenarioQuery query);

     CommonResult save(ScenarioVO model);

     CommonResult delete(Long[] id);

     CommonResult build(Long id) throws IOException;

     CommonResult copy(Long id, String merchantCode, String name, String label);

}
