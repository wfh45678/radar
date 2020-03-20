package com.pgmmers.radar.dal.model.impl;

import com.pgmmers.radar.dal.model.ModelConfDal;
import com.pgmmers.radar.mapper.ModelConfMapper;
import com.pgmmers.radar.mapper.ModelConfParamMapper;
import com.pgmmers.radar.model.ModelConfPO;
import com.pgmmers.radar.model.ModelConfParamPO;
import com.pgmmers.radar.vo.model.ModelConfParamVO;
import com.pgmmers.radar.vo.model.ModelConfVO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class ModelConfDalImpl implements ModelConfDal {

    @Resource
    private ModelConfMapper modelConfMapper;
    @Resource
    private ModelConfParamMapper modelConfParamMapper;

    @Override
    public ModelConfVO get(Long id) {
        ModelConfPO modelConfPO = modelConfMapper.selectByPrimaryKey(id);
        return convert(modelConfPO);
    }

    @Override
    public ModelConfVO getByModelId(Long modelId) {
        Example example = new Example(ModelConfPO.class);
        example.createCriteria().andEqualTo("modelId", modelId);
        ModelConfPO modelConfPO = modelConfMapper.selectOneByExample(example);
        return convert(modelConfPO);
    }

    @Override
    public ModelConfVO save(ModelConfVO confVO) {
        ModelConfPO conf = new ModelConfPO();
        BeanUtils.copyProperties(confVO, conf);
        if (conf.getId() == null) {
            conf.setCreateTime(new Date());
            conf.setUpdateDate(new Date());
            modelConfMapper.insert(conf);
            confVO.setId(conf.getId());
            ModelConfParamPO paramPO = new ModelConfParamPO();
            paramPO.setFeed(confVO.getConfParam().getFeed());
            paramPO.setExpressions(confVO.getConfParam().getExpressions());
            paramPO.setMoldId(conf.getId());
            modelConfParamMapper.insert(paramPO);
            confVO.getConfParam().setId(paramPO.getId());
        } else {
            modelConfMapper.updateByPrimaryKeySelective(conf);
        }

        return confVO;
    }

    @Override
    public ModelConfParamVO getParamById(Long id) {
        ModelConfParamVO modelConfParamVO = new ModelConfParamVO();
        ModelConfParamPO po = modelConfParamMapper.selectByPrimaryKey(id);
        BeanUtils.copyProperties(po, modelConfParamVO);
        return modelConfParamVO;
    }

    @Override
    public ModelConfParamVO saveParam(ModelConfParamVO paramVO) {
        ModelConfParamPO po = new ModelConfParamPO();
        BeanUtils.copyProperties(paramVO, po);
        if (po.getId() == null) {
            modelConfParamMapper.insert(po);
            paramVO.setId(po.getId());
        } else {
            modelConfParamMapper.updateByPrimaryKeySelective(po);
        }
        return paramVO;
    }

    private ModelConfVO convert(ModelConfPO modelConfPO) {
        ModelConfVO vo = null;
        if (modelConfPO != null) {
            vo = new ModelConfVO();
            BeanUtils.copyProperties(modelConfPO, vo);
            fitParams(vo);
        }
        return vo;
    }

    private void fitParams(ModelConfVO mold) {
        if (mold != null) {
            Example example = new Example(ModelConfParamPO.class);
            example.createCriteria().andEqualTo("moldId", mold.getId());
            List<ModelConfParamPO> moldParamList = modelConfParamMapper.selectByExample(example);
            List<ModelConfParamVO> list = moldParamList.stream().map(modelConfParamPO -> {
                ModelConfParamVO modelConfParamVO = new ModelConfParamVO();
                BeanUtils.copyProperties(modelConfParamPO, modelConfParamVO);
                return modelConfParamVO;
            }).collect(Collectors.toList());
            mold.setParams(list);
        }
    }
}
