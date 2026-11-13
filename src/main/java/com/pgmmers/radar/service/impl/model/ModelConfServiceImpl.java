package com.pgmmers.radar.service.impl.model;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pgmmers.radar.mapper.ModelConfMapper;
import com.pgmmers.radar.mapper.ModelConfParamMapper;
import com.pgmmers.radar.model.entity.ModelConfPO;
import com.pgmmers.radar.model.entity.ModelConfParamPO;
import com.pgmmers.radar.service.model.ModelConfService;
import com.pgmmers.radar.model.vo.model.ModelConfParamVO;
import com.pgmmers.radar.model.vo.model.ModelConfVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModelConfServiceImpl implements ModelConfService {
    @Resource
    private ModelConfMapper modelConfMapper;
    @Resource
    private ModelConfParamMapper modelConfParamMapper;

    /**
     * 原 ModelConfDalImpl.get 逻辑下沉。
     */
    @Override
    public ModelConfVO get(Long id) {
        ModelConfPO modelConfPO = modelConfMapper.selectById(id);
        return convert(modelConfPO);
    }

    /**
     * 原 ModelConfDalImpl.getByModelId 逻辑下沉。
     */
    @Override
    public ModelConfVO getByModelId(Long modelId) {
        ModelConfPO modelConfPO = modelConfMapper.selectOne(
                new LambdaQueryWrapper<ModelConfPO>().eq(ModelConfPO::getModelId, modelId));
        return convert(modelConfPO);
    }

    /**
     * 原 ModelConfDalImpl.save 逻辑下沉。
     */
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
            modelConfMapper.updateById(conf);
        }

        return confVO;
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
            List<ModelConfParamPO> moldParamList = modelConfParamMapper.selectList(
                    new LambdaQueryWrapper<ModelConfParamPO>()
                            .eq(ModelConfParamPO::getMoldId, mold.getId()));
            if (!CollectionUtils.isEmpty(moldParamList)) {
                List<ModelConfParamVO> list = moldParamList.stream().map(modelConfParamPO -> {
                    ModelConfParamVO modelConfParamVO = new ModelConfParamVO();
                    BeanUtils.copyProperties(modelConfParamPO, modelConfParamVO);
                    return modelConfParamVO;
                }).collect(Collectors.toList());
                mold.setParams(list);
            }
        }
    }
}
