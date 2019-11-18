package com.pgmmers.radar.dal.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.pgmmers.radar.model.*;
import com.pgmmers.radar.vo.data.MobileInfoVO;
import com.pgmmers.radar.vo.model.AbstractionVO;
import com.pgmmers.radar.vo.model.ModelVO;
import com.pgmmers.radar.vo.model.PreItemVO;
import com.pgmmers.radar.vo.model.RuleVO;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.IOException;


public class POVOUtils {

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static MobileInfoVO copyFromMobileInfPO(MobileInfoPO po) {
        MobileInfoVO vo = new MobileInfoVO();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }
    
    public static ModelVO copyFromModelPO(ModelPO po) {
        ModelVO vo = new ModelVO();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }
    
    public static MobileInfoPO copyFromMobileInfoVO(MobileInfoVO vo) {
        MobileInfoPO po = new MobileInfoPO();
        BeanUtils.copyProperties(vo, po);
        return po;
    }

    public static AbstractionVO copyFromAbstractPO(AbstractionPO po) {
        AbstractionVO vo = new AbstractionVO();
        BeanUtils.copyProperties(po, vo);
        JsonNode json = null;
        try {
            json = objectMapper.readTree(po.getRuleDefinition());
        } catch (Exception e) {
            e.printStackTrace();
        }
        vo.setRuleDefinition(json);
        return vo;
    }

    public static AbstractionPO copyFromAbstractVO(AbstractionVO vo) {
        AbstractionPO po = new AbstractionPO();
        BeanUtils.copyProperties(vo, po);
        try {
            String str = objectMapper.writeValueAsString(vo.getRuleDefinition());
            po.setRuleDefinition(str);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return po;
    }

    public static RuleVO copyFromRulePO(RulePO po) {
        RuleVO vo = new RuleVO();
        BeanUtils.copyProperties(po, vo);
        JsonNode json = null;
        try {
            json = objectMapper.readTree(po.getRuleDefinition());
        } catch (IOException e) {
            e.printStackTrace();
        }
        vo.setRuleDefinition(json);
        return vo;
    }

    public static RulePO copyFromRuleVO(RuleVO vo) {
        RulePO po = new RulePO();
        BeanUtils.copyProperties(vo, po);
        try {
            String str = objectMapper.writeValueAsString(vo.getRuleDefinition());
            po.setRuleDefinition(str);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return po;
    }

    public static PreItemPO copyFromPreItemVO(PreItemVO vo) {
        PreItemPO po = new PreItemPO();
        BeanUtils.copyProperties(vo, po);
        try {
            if (vo.getConfigJson() != null) {
                String str = objectMapper.writeValueAsString(vo.getConfigJson());
                po.setConfigJson(str);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return po;
    }

    public static PreItemVO copyFromPreItemPO(PreItemPO po) {
        PreItemVO vo = new PreItemVO();
        BeanUtils.copyProperties(po, vo);
        JsonNode json = null;
        try {
            if (!StringUtils.isEmpty(po.getConfigJson())) {
                json = objectMapper.readTree(po.getConfigJson());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        vo.setConfigJson(json);
        return vo;
    }
}
