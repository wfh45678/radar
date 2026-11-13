package com.pgmmers.radar.service.impl.model;

import com.pgmmers.radar.mapper.ModelConfParamMapper;
import com.pgmmers.radar.model.entity.ModelConfParamPO;
import com.pgmmers.radar.service.model.ModelConfParamService;
import com.pgmmers.radar.model.vo.model.ModelConfParamVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModelConfParamServiceImpl implements ModelConfParamService {

    @Autowired
    private ModelConfParamMapper modelConfParamMapper;

    /**
     * 原 ModelConfDalImpl.getParamById 逻辑下沉。
     */
    @Override
    public ModelConfParamVO get(Long id) {
        ModelConfParamVO modelConfParamVO = new ModelConfParamVO();
        ModelConfParamPO po = modelConfParamMapper.selectById(id);
        if (po != null) {
            BeanUtils.copyProperties(po, modelConfParamVO);
        }
        return modelConfParamVO;
    }

    /**
     * 原 ModelConfDalImpl.saveParam 逻辑下沉。
     */
    @Override
    public ModelConfParamVO save(ModelConfParamVO modelConfParam) {
        ModelConfParamPO po = new ModelConfParamPO();
        BeanUtils.copyProperties(modelConfParam, po);
        if (po.getId() == null) {
            modelConfParamMapper.insert(po);
            modelConfParam.setId(po.getId());
        } else {
            modelConfParamMapper.updateById(po);
        }
        return modelConfParam;
    }
}
