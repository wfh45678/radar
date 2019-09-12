package com.pgmmers.radar.service.model;



import com.pgmmers.radar.dal.bean.PreItemQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.model.PreItemVO;

import java.util.List;

public interface PreItemService {

     List<PreItemVO> listPreItem(Long modelId);

     PreItemVO get(Long id);

     CommonResult query(PreItemQuery query);

     CommonResult save(PreItemVO preItem);

     CommonResult delete(Long[] id);

}
