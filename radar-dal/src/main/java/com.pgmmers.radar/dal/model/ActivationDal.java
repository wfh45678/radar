package com.pgmmers.radar.dal.model;


import com.pgmmers.radar.dal.bean.ActivationQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.vo.model.ActivationVO;

public interface ActivationDal {

	 ActivationVO get(Long id);

	 PageResult<ActivationVO> query(ActivationQuery query);

	 int save(ActivationVO model);

	 int delete(Long[] id);

}
