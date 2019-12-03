package com.pgmmers.radar.dal.model.impl;

import com.pgmmers.radar.dal.model.MoldDal;
import com.pgmmers.radar.mapper.MoldMapper;
import com.pgmmers.radar.mapper.MoldParamMapper;
import com.pgmmers.radar.model.MoldPO;
import com.pgmmers.radar.model.MoldParamPO;
import com.pgmmers.radar.vo.model.MoldParamVO;
import com.pgmmers.radar.vo.model.MoldVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoldDalImpl implements MoldDal {
    @Resource
    private MoldMapper moldMapper;
    @Resource
    private MoldParamMapper moldParamMapper;

    @Override
    public MoldVO get(Long id) {
        MoldPO moldPO = moldMapper.selectByPrimaryKey(id);
        return convert(moldPO);
    }

    @Override
    public MoldVO getByModelId(Long modelId) {
        Example example = new Example(MoldPO.class);
        example.createCriteria().andEqualTo("modelId", modelId);
        MoldPO moldPO = moldMapper.selectOneByExample(example);
        return convert(moldPO);
    }

    private MoldVO convert(MoldPO moldPO) {
        MoldVO vo = null;
        if (moldPO != null) {
            vo = new MoldVO();
            BeanUtils.copyProperties(moldPO, vo);
            fitParams(vo);
        }
        return vo;
    }

    private void fitParams(MoldVO mold) {
        if (mold != null) {
            Example example = new Example(MoldParamPO.class);
            example.createCriteria().andEqualTo("moldId", mold.getId());
            List<MoldParamPO> moldParamList = moldParamMapper.selectByExample(example);
            List<MoldParamVO> list = moldParamList.stream().map(moldParamPO -> {
                MoldParamVO moldParamVO = new MoldParamVO();
                BeanUtils.copyProperties(moldParamPO, moldParamVO);
                return moldParamVO;
            }).collect(Collectors.toList());
            mold.setParams(list);
        }
    }
}
