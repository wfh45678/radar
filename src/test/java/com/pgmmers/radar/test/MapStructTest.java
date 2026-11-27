package com.pgmmers.radar.test;

import com.pgmmers.radar.mapper.AbstractionMapper;
import com.pgmmers.radar.mapper.StrategyMapper;
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
import com.pgmmers.radar.converter.AbstractionMapping;
import com.pgmmers.radar.converter.StrategyMapping;
import com.pgmmers.radar.converter.DataListMetaMapping;
import com.pgmmers.radar.converter.DataListRecordMapping;
import com.pgmmers.radar.converter.DataListsMapping;
import com.pgmmers.radar.converter.FieldMapping;
import com.pgmmers.radar.converter.MobileInfoMapping;
import com.pgmmers.radar.converter.ModelMapping;
import com.pgmmers.radar.converter.PreItemMapping;
import com.pgmmers.radar.converter.RuleHistoryMapping;
import com.pgmmers.radar.converter.RuleMapping;
import com.pgmmers.radar.converter.UserMapping;
import com.pgmmers.radar.model.entity.AbstractionPO;
import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.entity.DataListMetaPO;
import com.pgmmers.radar.model.entity.DataListRecordPO;
import com.pgmmers.radar.model.entity.DataListsPO;
import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.entity.MobileInfoPO;
import com.pgmmers.radar.model.entity.ModelPO;
import com.pgmmers.radar.model.entity.PreItemPO;
import com.pgmmers.radar.model.entity.RuleHistoryPO;
import com.pgmmers.radar.model.entity.RulePO;
import com.pgmmers.radar.model.entity.UserPO;
import com.pgmmers.radar.model.vo.admin.UserVO;
import com.pgmmers.radar.model.vo.data.MobileInfoVO;
import com.pgmmers.radar.model.vo.model.AbstractionVO;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.model.vo.model.DataListMetaVO;
import com.pgmmers.radar.model.vo.model.DataListRecordVO;
import com.pgmmers.radar.model.vo.model.DataListsVO;
import com.pgmmers.radar.model.vo.model.FieldVO;
import com.pgmmers.radar.model.vo.model.ModelVO;
import com.pgmmers.radar.model.vo.model.PreItemVO;
import com.pgmmers.radar.model.vo.model.RuleHistoryVO;
import com.pgmmers.radar.model.vo.model.RuleVO;
import com.pgmmers.radar.support.AbstractIntegrationTest;
import java.util.Date;
import java.util.List;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * author: wangcheng Date: 2020/3/17 Time: 下午5:19 Description:
 * L3 集成测试：MapStruct 转换 + mapper 读链路（依赖 AbstractIntegrationTest 四件套容器）。
 */
public class MapStructTest extends AbstractIntegrationTest {


    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ModelMapping modelMapping;

    @Resource
    private AbstractionMapping abstractionMapping;
    @Autowired
    private AbstractionMapper abstractionMapper;
    @Resource
    private StrategyMapping strategyMapping;
    @Autowired
    private StrategyMapper strategyMapper;
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
        List<ModelPO> pos = modelMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<ModelVO> vos = modelMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<ModelPO> pos1 = modelMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
    }

    @Test
    public void abstractionTest() {
        List<AbstractionPO> pos = abstractionMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<AbstractionVO> vos = abstractionMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<AbstractionPO> pos1 = abstractionMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
    }

    @Test
    public void strategyTest() {
        List<StrategyPO> pos = strategyMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<StrategyVO> vos = strategyMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<StrategyPO> pos1 = strategyMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);

    }

    @Test
    public void fieldTest() {
        List<FieldPO> pos = fieldMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<FieldVO> vos = fieldMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<FieldPO> pos1 = fieldMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
    }

    @Test
    public void ruleTest() {
        List<RulePO> pos = ruleMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<RuleVO> vos = ruleMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<RulePO> pos1 = ruleMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
    }

    @Resource
    DataListMetaMapping dataListMetaMapping;
    @Autowired
    DataListMetaMapper dataListMetaMapper;

    @Test
    public void datalistMetaTest() {
        List<DataListMetaPO> pos = dataListMetaMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<DataListMetaVO> vos = dataListMetaMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<DataListMetaPO> pos1 = dataListMetaMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
    }

    @Resource
    private DataListRecordMapping dataListRecordMapping;
    @Autowired
    private DataListRecordMapper dataListRecordMapper;

    @Test
    public void dataListRecordTest() {
        List<DataListRecordPO> pos = dataListRecordMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<DataListRecordVO> vos = dataListRecordMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<DataListRecordPO> pos1 = dataListRecordMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
        ;
    }

    @Resource
    private DataListsMapping dataListsMapping;
    @Autowired
    private DataListsMapper dataListsMapper;

    @Test
    public void dataListsTest() {
        List<DataListsPO> pos = dataListsMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<DataListsVO> vos = dataListsMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<DataListsPO> pos1 = dataListsMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
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
        Assertions.assertNotNull(vos);
        MobileInfoPO pos1 = mobileInfoMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
    }

    @Resource
    private PreItemMapping preItemMapping;
    @Autowired
    private PreItemMapper preItemMapper;

    @Test
    public void preItemTest() {
        List<PreItemPO> pos = preItemMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<PreItemVO> vos = preItemMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<PreItemPO> pos1 = preItemMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
    }

    @Resource
    private RuleHistoryMapping ruleHistoryMapping;
    @Autowired
    private RuleHistoryMapper ruleHistoryMapper;

    @Test
    public void ruleHistoryTest() {
        List<RuleHistoryPO> pos = ruleHistoryMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<RuleHistoryVO> vos = ruleHistoryMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<RuleHistoryPO> pos1 = ruleHistoryMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
    }

    @Resource
    private UserMapping userMapping;
    @Autowired
    private UserMapper userMapper;

    @Test
    public void userTest() {
        List<UserPO> pos = userMapper.selectList(null);
        Assertions.assertNotNull(pos);
        List<UserVO> vos = userMapping.sourceToTarget(pos);
        Assertions.assertNotNull(vos);
        List<UserPO> pos1 = userMapping.targetToSource(vos);
        Assertions.assertNotNull(pos1);
    }

}
