package com.pgmmers.radar.service.impl.model.transfer;

import com.pgmmers.radar.mapper.AbstractionMapper;
import com.pgmmers.radar.mapper.StrategyMapper;
import com.pgmmers.radar.mapper.DataListMetaMapper;
import com.pgmmers.radar.mapper.DataListRecordMapper;
import com.pgmmers.radar.mapper.DataListsMapper;
import com.pgmmers.radar.mapper.FieldMapper;
import com.pgmmers.radar.mapper.PreItemMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.model.entity.AbstractionPO;
import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.entity.DataListMetaPO;
import com.pgmmers.radar.model.entity.DataListRecordPO;
import com.pgmmers.radar.model.entity.DataListsPO;
import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.entity.PreItemPO;
import com.pgmmers.radar.model.entity.RulePO;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 模型对象图装配器（v3.2）：单实例 copy 与策略包导入共用的装配段——
 * 源数据（DB 行 | 信封反序列化结果）在**新模型 id 下整图落库**，并完成
 * strategyId / dataListId 的插入后重映射。
 *
 * <p>单事务包裹：任一插入失败整图回滚，不允许"半个模型"。
 * 范围与 copyModel 原逻辑一致（不含 engine_model_conf——ML 休眠配置）。</p>
 *
 * @since 3.2.0
 */
@Component
public class ModelGraphAssembler {

    @Autowired
    private FieldMapper fieldMapper;
    @Autowired
    private PreItemMapper preItemMapper;
    @Autowired
    private DataListsMapper dataListMapper;
    @Autowired
    private DataListMetaMapper dataListMetaMapper;
    @Autowired
    private DataListRecordMapper dataListRecordMapper;
    @Autowired
    private AbstractionMapper abstractionMapper;
    @Autowired
    private StrategyMapper strategyMapper;
    @Autowired
    private RuleMapper ruleMapper;

    /**
     * 把源对象图装配到 newModelId 下（新模型行由调用方先行插入）。
     *
     * @return 成功插入的子对象总数
     */
    @Transactional
    public int assemble(Long newModelId,
                        List<FieldPO> fields,
                        List<PreItemPO> preItems,
                        List<DataListsPO> dataLists,
                        List<DataListMetaPO> dataListMeta,
                        List<DataListRecordPO> dataListRecords,
                        List<AbstractionPO> abstractions,
                        List<StrategyPO> strategies,
                        List<RulePO> rules) {
        Date sysDate = new Date();
        int count = 0;

        // 字段
        for (FieldPO po : fields) {
            po.setModelId(newModelId);
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            fieldMapper.insert(po);
            count++;
        }

        // preItem
        for (PreItemPO po : preItems) {
            po.setModelId(newModelId);
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            preItemMapper.insert(po);
            count++;
        }

        // 数据名单（+meta+record，dataListId 重映射）
        Map<Long, Long> dataListIdMap = new HashMap<>();
        for (DataListsPO po : dataLists) {
            Long oldDataListId = po.getId();
            po.setModelId(newModelId);
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            dataListMapper.insert(po);
            dataListIdMap.put(oldDataListId, po.getId());
            count++;
        }
        for (DataListMetaPO po : dataListMeta) {
            po.setDataListId(dataListIdMap.get(po.getDataListId()));
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            dataListMetaMapper.insert(po);
            count++;
        }
        for (DataListRecordPO po : dataListRecords) {
            po.setDataListId(dataListIdMap.get(po.getDataListId()));
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            dataListRecordMapper.insert(po);
            count++;
        }

        // 抽象
        for (AbstractionPO po : abstractions) {
            po.setModelId(newModelId);
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            abstractionMapper.insert(po);
            count++;
        }

        // 激活（+规则，strategyId 重映射）
        Map<Long, Long> strategyIdMap = new HashMap<>();
        for (StrategyPO po : strategies) {
            Long oldStrategyId = po.getId();
            po.setModelId(newModelId);
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            strategyMapper.insert(po);
            strategyIdMap.put(oldStrategyId, po.getId());
            count++;
        }
        for (RulePO po : rules) {
            po.setModelId(newModelId);
            po.setStrategyId(strategyIdMap.get(po.getStrategyId()));
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            ruleMapper.insert(po);
            count++;
        }
        return count;
    }
}
