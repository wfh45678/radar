package com.pgmmers.radar.dal.model;

import com.pgmmers.radar.dal.bean.ModelQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.vo.model.*;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;



public interface ModelDal {

     ModelVO getModelByGuid(String guid);

     ModelVO getModelById(Long modelId);
    
     List<ModelVO> listModel(String merchantCode,Integer status);
    
     List<ModelVO> listModel(String merchantCode,Integer status, Boolean isTemplate);
    
     List<ModelVO> listModel(Integer status);
    
     List<FieldVO> listField(Long modelId);
    
     List<AbstractionVO> listAbstraction(Long modelId);
    
     List<AbstractionVO> listAbstraction(Long modelId, Integer status);
    
     List<ActivationVO> listActivation(Long modelId);
    
     List<ActivationVO> listActivation(Long modelId, Integer status);
    
    
     List<RuleVO> listRules(Long modelId, Long activationId, Integer status);
    
     List<PreItemVO> listPreItem(Long modelId, Integer status);

	 PageResult<ModelVO> query(ModelQuery query);

	 int save(ModelVO model);

	 int delete(Long[] id);
	
	 int copy(ModelVO model);

}
