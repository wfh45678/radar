package com.pgmmers.radar.dal.data.impl;

import java.util.ArrayList;
import java.util.List;

import com.pgmmers.radar.dal.data.MobileInfoDal;
import com.pgmmers.radar.dal.util.POVOUtils;
import com.pgmmers.radar.mapper.MobileInfoMapper;
import com.pgmmers.radar.model.MobileInfoPO;
import com.pgmmers.radar.vo.data.MobileInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.pagehelper.PageHelper;

import tk.mybatis.mapper.entity.Example;

@Service
public class MobileInfoDalImpl implements MobileInfoDal {

    @Autowired
    private MobileInfoMapper mobileMapper;

    @Override
    public List<MobileInfoVO> listMobileInfo(int pageNum, int pageSize) {
        Example example = new Example(MobileInfoPO.class);
        PageHelper.startPage(pageNum, pageSize);
        List<MobileInfoPO> poList = mobileMapper.selectByExample(example);
        List<MobileInfoVO> voList = new ArrayList<MobileInfoVO>();
        MobileInfoVO vo;
        for (MobileInfoPO po : poList) {
            vo = POVOUtils.copyFromMobileInfPO(po);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public int countQty(MobileInfoVO vo) {
        Example example = new Example(MobileInfoPO.class);
        int count = mobileMapper.selectCountByExample(example);
        return count;
    }

    @Override
    public MobileInfoVO getMobileInfoByMobileNo(String mobile) {
        Example example = new Example(MobileInfoPO.class);
        example.createCriteria().andEqualTo("mobile", mobile);
        List<MobileInfoPO> list = mobileMapper.selectByExample(example);
        if (list != null && list.size() > 0) {
            return POVOUtils.copyFromMobileInfPO(list.get(0));
        }
        return null;
    }

    @Override
    public List<MobileInfoVO> listMobileInfo(String province, String city, int pageNum, int pageSize) {
        Example example = new Example(MobileInfoPO.class);
        PageHelper.startPage(pageNum, pageSize);
        Example.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(province)) {
            criteria.andEqualTo("province", province);
        }
        
        if (!StringUtils.isEmpty(city)) {
            criteria.andEqualTo("city", city);
        }
        
        List<MobileInfoPO> poList = mobileMapper.selectByExample(example);
        List<MobileInfoVO> voList = new ArrayList<MobileInfoVO>();
        MobileInfoVO vo;
        for (MobileInfoPO po : poList) {
            vo = POVOUtils.copyFromMobileInfPO(po);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public int save(MobileInfoVO info) {
        MobileInfoPO po = POVOUtils.copyFromMobileInfoVO(info);
        return mobileMapper.insertSelective(po);
    }

}
