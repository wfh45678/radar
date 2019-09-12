package com.pgmmers.radar.dal.model;


import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.PreItemQuery;
import com.pgmmers.radar.vo.model.PreItemVO;

public interface PreItemDal {

     PreItemVO get(Long id);

     PageResult<PreItemVO> query(PreItemQuery query);

     int save(PreItemVO preItem);

     int delete(Long[] id);

}
