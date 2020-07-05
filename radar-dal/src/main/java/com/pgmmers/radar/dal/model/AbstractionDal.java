package com.pgmmers.radar.dal.model;

import java.util.List;


import com.pgmmers.radar.dal.bean.AbstractionQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.vo.model.AbstractionVO;

public interface AbstractionDal {

    public AbstractionVO get(Long id);

    public List<AbstractionVO> list(Long modelId);

    public PageResult<AbstractionVO> query(AbstractionQuery query);

    public int save(AbstractionVO abstraction);

    public int delete(Long[] id);

}
