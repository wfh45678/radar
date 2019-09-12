package com.pgmmers.radar.dal.model;


import com.pgmmers.radar.dal.bean.FieldQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.vo.model.FieldVO;
import org.springframework.cache.annotation.Cacheable;

public interface FieldDal {

     FieldVO get(Long id);

     PageResult<FieldVO> query(FieldQuery query);

     int save(FieldVO field);

     int delete(Long[] id);

}
