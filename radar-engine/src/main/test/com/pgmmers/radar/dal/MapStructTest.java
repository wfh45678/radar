package com.pgmmers.radar.dal;

import com.pgmmers.radar.EngineApplication;
import com.pgmmers.radar.mapper.AbstractionMapper;
import com.pgmmers.radar.mapper.ActivationMapper;
import com.pgmmers.radar.mapper.DataListMetaMapper;
import com.pgmmers.radar.mapper.DataListRecordMapper;
import com.pgmmers.radar.mapper.DataListsMapper;
import com.pgmmers.radar.mapper.FieldMapper;
import com.pgmmers.radar.mapper.MobileInfoMapper;
import com.pgmmers.radar.mapper.ModelMapper;
import com.pgmmers.radar.mapper.PreItemMapper;
import com.pgmmers.radar.mapper.RuleHistoryMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.mapper.UserMapper;
import com.pgmmers.radar.mapstruct.AbstractionMapping;
import com.pgmmers.radar.mapstruct.ActivationMapping;
import com.pgmmers.radar.mapstruct.DataListMetaMapping;
import com.pgmmers.radar.mapstruct.DataListRecordMapping;
import com.pgmmers.radar.mapstruct.DataListsMapping;
import com.pgmmers.radar.mapstruct.FieldMapping;
import com.pgmmers.radar.mapstruct.MobileInfoMapping;
import com.pgmmers.radar.mapstruct.ModelMapping;
import com.pgmmers.radar.mapstruct.PreItemMapping;
import com.pgmmers.radar.mapstruct.RuleHistoryMapping;
import com.pgmmers.radar.mapstruct.RuleMapping;
import com.pgmmers.radar.mapstruct.UserMapping;
import com.pgmmers.radar.model.AbstractionPO;
import com.pgmmers.radar.model.ActivationPO;
import com.pgmmers.radar.model.DataListMetaPO;
import com.pgmmers.radar.model.DataListRecordPO;
import com.pgmmers.radar.model.DataListsPO;
import com.pgmmers.radar.model.FieldPO;
import com.pgmmers.radar.model.MobileInfoPO;
import com.pgmmers.radar.model.ModelPO;
import com.pgmmers.radar.model.PreItemPO;
import com.pgmmers.radar.model.RuleHistoryPO;
import com.pgmmers.radar.model.RulePO;
import com.pgmmers.radar.model.UserPO;
import com.pgmmers.radar.vo.admin.UserVO;
import com.pgmmers.radar.vo.data.MobileInfoVO;
import com.pgmmers.radar.vo.model.AbstractionVO;
import com.pgmmers.radar.vo.model.ActivationVO;
import com.pgmmers.radar.vo.model.DataListMetaVO;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import com.pgmmers.radar.vo.model.DataListsVO;
import com.pgmmers.radar.vo.model.FieldVO;
import com.pgmmers.radar.vo.model.ModelVO;
import com.pgmmers.radar.vo.model.PreItemVO;
import com.pgmmers.radar.vo.model.RuleHistoryVO;
import com.pgmmers.radar.vo.model.RuleVO;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * author: wangcheng Date: 2020/3/17 Time: 下午5:19 Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EngineApplication.class)
public class MapStructTest {


    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ModelMapping modelMapping;

    @Resource
    private AbstractionMapping abstractionMapping;
    @Autowired
    private AbstractionMapper abstractionMapper;
    @Resource
    private ActivationMapping activationMapping;
    @Autowired
    private ActivationMapper activationMapper;
    @Resource
    private FieldMapping fieldMapping;
    @Autowired
    private FieldMapper fieldMapper;
    @Resource
    private RuleMapping ruleMapping;
    @Autowired
    private RuleMapper ruleMapper;

    @Test
    public void modelTest() {
        List<ModelPO> pos = modelMapper.selectAll();
        Assert.assertNotNull(pos);
        List<ModelVO> vos = modelMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<ModelPO> pos1 = modelMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
    }

    @Test
    public void abstractionTest() {
        List<AbstractionPO> pos = abstractionMapper.selectAll();
        Assert.assertNotNull(pos);
        List<AbstractionVO> vos = abstractionMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<AbstractionPO> pos1 = abstractionMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
    }

    @Test
    public void activationTest() {
        List<ActivationPO> pos = activationMapper.selectAll();
        Assert.assertNotNull(pos);
        List<ActivationVO> vos = activationMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<ActivationPO> pos1 = activationMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);

    }

    @Test
    public void fieldTest() {
        List<FieldPO> pos = fieldMapper.selectAll();
        Assert.assertNotNull(pos);
        List<FieldVO> vos = fieldMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<FieldPO> pos1 = fieldMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
    }

    @Test
    public void ruleTest() {
        List<RulePO> pos = ruleMapper.selectAll();
        Assert.assertNotNull(pos);
        List<RuleVO> vos = ruleMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<RulePO> pos1 = ruleMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
    }

    @Resource
    DataListMetaMapping dataListMetaMapping;
    @Autowired
    DataListMetaMapper dataListMetaMapper;

    @Test
    public void datalistMetaTest() {
        List<DataListMetaPO> pos = dataListMetaMapper.selectAll();
        Assert.assertNotNull(pos);
        List<DataListMetaVO> vos = dataListMetaMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<DataListMetaPO> pos1 = dataListMetaMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
    }

    @Resource
    private DataListRecordMapping dataListRecordMapping;
    @Autowired
    private DataListRecordMapper dataListRecordMapper;

    @Test
    public void dataListRecordTest() {
        List<DataListRecordPO> pos = dataListRecordMapper.selectAll();
        Assert.assertNotNull(pos);
        List<DataListRecordVO> vos = dataListRecordMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<DataListRecordPO> pos1 = dataListRecordMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
        ;
    }

    @Resource
    private DataListsMapping dataListsMapping;
    @Autowired
    private DataListsMapper dataListsMapper;

    @Test
    public void dataListsTest() {
        List<DataListsPO> pos = dataListsMapper.selectAll();
        Assert.assertNotNull(pos);
        List<DataListsVO> vos = dataListsMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<DataListsPO> pos1 = dataListsMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
    }

    @Resource
    private MobileInfoMapping mobileInfoMapping;
    @Autowired
    private MobileInfoMapper mobileInfoMapper;

    @Test
    public void mobileInfoTest() {
        MobileInfoPO po = new MobileInfoPO();
        po.setCity("hangzhou");
        po.setCreateTime(new Date());
        po.setId(1L);
        po.setMobile("1234");
        po.setProvince("zj");
        po.setRegionCode("11");
        po.setSupplier("1");
        po.setUpdateTime(new Date());
        MobileInfoVO vos = mobileInfoMapping.sourceToTarget(po);
        Assert.assertNotNull(vos);
        MobileInfoPO pos1 = mobileInfoMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
    }

    @Resource
    private PreItemMapping preItemMapping;
    @Autowired
    private PreItemMapper preItemMapper;

    @Test
    public void preItemTest() {
        List<PreItemPO> pos = preItemMapper.selectAll();
        Assert.assertNotNull(pos);
        List<PreItemVO> vos = preItemMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<PreItemPO> pos1 = preItemMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
    }

    @Resource
    private RuleHistoryMapping ruleHistoryMapping;
    @Autowired
    private RuleHistoryMapper ruleHistoryMapper;

    @Test
    public void ruleHistoryTest() {
        List<RuleHistoryPO> pos = ruleHistoryMapper.selectAll();
        Assert.assertNotNull(pos);
        List<RuleHistoryVO> vos = ruleHistoryMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<RuleHistoryPO> pos1 = ruleHistoryMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
    }

    @Resource
    private UserMapping userMapping;
    @Autowired
    private UserMapper userMapper;

    @Test
    public void userTest() {
        List<UserPO> pos = userMapper.selectAll();
        Assert.assertNotNull(pos);
        List<UserVO> vos = userMapping.sourceToTarget(pos);
        Assert.assertNotNull(vos);
        List<UserPO> pos1 = userMapping.targetToSource(vos);
        Assert.assertNotNull(pos1);
    }

}
