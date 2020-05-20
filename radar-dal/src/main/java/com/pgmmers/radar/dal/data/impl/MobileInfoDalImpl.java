package com.pgmmers.radar.dal.data.impl;

import com.github.pagehelper.PageHelper;
import com.pgmmers.radar.dal.data.MobileInfoDal;
import com.pgmmers.radar.mapper.MobileInfoMapper;
import com.pgmmers.radar.mapstruct.MobileInfoMapping;
import com.pgmmers.radar.model.MobileInfoPO;
import com.pgmmers.radar.vo.data.MobileInfoVO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

@Service
public class MobileInfoDalImpl implements MobileInfoDal {

    @Autowired
    private MobileInfoMapper mobileMapper;
    @Resource
    private MobileInfoMapping mobileInfoVOMapping;

    @Override
    public List<MobileInfoVO> listMobileInfo(int pageNum, int pageSize) {
        Example example = new Example(MobileInfoPO.class);
        PageHelper.startPage(pageNum, pageSize);
        List<MobileInfoPO> poList = mobileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(poList)) {
            return new ArrayList<>();
        }
        return mobileInfoVOMapping.sourceToTarget(poList);
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
            return mobileInfoVOMapping.sourceToTarget(list.get(0));
        }
        return null;
    }

    @Override
    public List<MobileInfoVO> listMobileInfo(String province, String city, int pageNum,
            int pageSize) {
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
        List<MobileInfoVO> voList;
        if (CollectionUtils.isEmpty(poList)) {
            voList = new ArrayList<>();
        } else {
            voList=mobileInfoVOMapping.sourceToTarget(poList);
        }
        return voList;
    }

    @Override
    public int save(MobileInfoVO info) {
        MobileInfoPO po = mobileInfoVOMapping.targetToSource(info);
        return mobileMapper.insertSelective(po);
    }

}
