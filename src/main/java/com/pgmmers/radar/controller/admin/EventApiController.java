/*
 * Copyright (c) 2019 WangFeiHu
 *  Radar is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *  http://license.coscl.org.cn/MulanPSL2
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 */

package com.pgmmers.radar.controller.admin;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.pgmmers.radar.model.query.EventExportQuery;
import com.pgmmers.radar.model.query.EventQuery;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.model.query.TermQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.vo.DataColumnInfo;
import com.pgmmers.radar.service.enums.DataType;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.impl.engine.plugin.PluginEntry;
import org.apache.commons.lang3.StringUtils;
import com.pgmmers.radar.service.logs.EventService;
import com.pgmmers.radar.service.model.StrategyService;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.service.model.FeatureService;
import com.pgmmers.radar.service.model.RuleService;
import com.pgmmers.radar.model.vo.common.FieldValueVO;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.model.vo.model.FieldVO;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.model.vo.model.RuleVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 以后会独立拆分到分析子项目里面去。
 * @author  feihu.wang
 */
@RestController
@RequestMapping("/services/v1/event")
@Tag(name = "事件信息检索分析API", description = "事件信息检索接口相关操作")
public class EventApiController {

    @Autowired
    private EventService eventService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private FeatureService featureService;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private RuleService ruleService;
    @Autowired
    private PluginManager pluginManager;


    @PostMapping("/query")
    public CommonResult query(@RequestBody EventQuery query) throws IOException {
        CommonResult result = new CommonResult();
        List<Object> list = eventService.query(query);
        result.getData().put("page", list);
        result.setSuccess(true);
        return result;
    }


    @PostMapping("/search")
    public CommonResult search(@RequestBody TermQuery term, HttpSession session) {
        CommonResult result = new CommonResult();
        PageResult<Object> page ;
        page = eventService.query(term);
        if (page != null) {
            result.getData().put("page", page);
            result.setSuccess(true);
        } else {
            page = new PageResult<>(1, 10, 0, new ArrayList<>());
            result.getData().put("page", page);
            result.setSuccess(true);
            result.setMsg("查询失败");
        }
        session.setAttribute("searchTerm", term);
        return result;
    }


    @PostMapping("/export")
    public CommonResult exportExcel(EventExportQuery query, HttpSession session) {
        session.setAttribute("exportQuery", query);
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        return result;
    }

    @GetMapping("/download")
    public void downloadExcel(HttpServletRequest request, HttpServletResponse response) {
        TermQuery term = (TermQuery)  request.getSession().getAttribute("searchTerm");
        if (term == null || term.getScenarioId() == null) {
            return;
        }
        EventExportQuery query = (EventExportQuery) request.getSession().getAttribute("exportQuery");

        if (query == null) {
            return;
        }
        String[] fieldIds = query.getFields();
        String[] features = query.getFeatures();
        String[] strategies = query.getStrategies();
        String[] rules = query.getRules();

        Map<String, String> fieldIdMap = new HashMap<>();
        Map<String, String> itemsIdMap = new HashMap<>();
        Map<String, String> actIdMap = new HashMap<>();
        Map<String, String> ruleIdMap = new HashMap<>();

        for (int i = 0; i < fieldIds.length; i++) {
            fieldIdMap.put(fieldIds[i], "");
        }

        for (int i = 0; i < features.length; i++) {
            itemsIdMap.put(features[i], "");
        }

        for (int i = 0; i < strategies.length; i++) {
            actIdMap.put(strategies[i], "");
        }

        for (int i = 0; i < rules.length; i++) {
            ruleIdMap.put(rules[i], "");
        }

        term.setPageNo(1);
        // 暂时限制最大导出1W条
        term.setPageSize(10000);

        PageResult<Object> page = eventService.query(term);
        List<Object> records = page.getList();

        List<FieldVO> fieldList = fieldService.listField(term.getScenarioId());
        List<FeatureVO> featureList = featureService.listFeature(term
                .getScenarioId());
        List<StrategyVO> strategyList = strategyService
                .listStrategy(term.getScenarioId());
        List<RuleVO> ruleList = ruleService
                .listRuleByScenarioId(term.getScenarioId());

        // 关联 rule id　与　strategy
        for (StrategyVO act : strategyList) {
            List<RuleVO> ruleVOList = ruleService.listRule(act.getId());
            for (RuleVO rule : ruleVOList) {
                if (ruleIdMap.containsKey(rule.getName())) {
                    ruleIdMap.put(rule.getName(), act.getStrategyName());
                }
            }
        }

        // 设置title 和 字段 key
        List<String> keyList4Field = new ArrayList<>();
        List<String> titleList4Field = new ArrayList<>();

        for (FieldVO field : fieldList) {
            if (!fieldIdMap.containsKey(field.getFieldName())) {
                continue;
            }
            keyList4Field.add(field.getFieldName());
            titleList4Field.add(field.getLabel());
        }

        // feature fields
        List<Object> keyList4Item = new ArrayList<>();
        List<String> titleList4Item = new ArrayList<>();
        for (FeatureVO feature : featureList) {
            if (!itemsIdMap.containsKey(feature.getFeatureName())) {
                continue;
            }
            com.fasterxml.jackson.databind.JsonNode output = feature.getOutput();
            if (output != null && output.isArray() && !output.isEmpty()) {
                // 多列特征：导出列为 features.<name>.<column>
                for (com.fasterxml.jackson.databind.JsonNode col : output) {
                    String column = col.path("column").asText("");
                    if (column.isEmpty()) {
                        continue;
                    }
                    keyList4Item.add(new FieldValueVO(feature.getFeatureName(), column));
                    titleList4Item.add(feature.getLabel() + "."
                            + col.path("title").asText(column));
                }
            } else {
                keyList4Item.add(feature.getFeatureName());
                titleList4Item.add(feature.getLabel());
            }
        }

        // strategy field
        List<Object> keyList4Act = new ArrayList<>();
        List<String> titleList4Act = new ArrayList<>();
        for (StrategyVO act : strategyList) {
            if (!actIdMap.containsKey(act.getStrategyName())) {
                continue;
            }
            keyList4Act.add(new FieldValueVO(act.getStrategyName(), "risk"));
            keyList4Act.add(new FieldValueVO(act.getStrategyName(), "score"));
            titleList4Act.add(act.getLabel() + "." + "风险级别");
            titleList4Act.add(act.getLabel() + "." + "分数");
        }

        // rule field
        List<Object> keyList4Rule = new ArrayList<>();
        List<String> titleList4Rule = new ArrayList<>();
        for (RuleVO rule : ruleList) {
            if (!ruleIdMap.containsKey(rule.getName())) {
                continue;
            }
            // keyList4Rule.add(new FieldValueVO(ruleIdMap.get(rule.getId()),
            // "rule_" + rule.getId(), "key"));
            keyList4Rule.add(new FieldValueVO(ruleIdMap.get(rule.getName()),
                    "rule_" + rule.getId(), "desc"));
            keyList4Rule.add(new FieldValueVO(ruleIdMap.get(rule.getName()),
                    "rule_" + rule.getId(), "value"));

            // titleList4Rule.add(rule.getLabel() + ".key");
            titleList4Rule.add(rule.getLabel() + ".desc");
            titleList4Rule.add(rule.getLabel() + ".value");

        }

        Workbook workbook = eventService.createExcel(records, keyList4Field,
                titleList4Field, keyList4Item, titleList4Item, keyList4Act,
                titleList4Act, keyList4Rule, titleList4Rule);
        String file = "e:\\tmp\\" + System.currentTimeMillis() + ".xlsx";
        FileOutputStream fos = null;

        // 设置相应类型,告诉浏览器输出的内容为图片
        response.setContentType("application/ms-excel");
        // 设置响应头信息，告诉浏览器不要缓存此内容
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expire", 0);
        OutputStream os = null;
        try {
            // fos = new FileOutputStream(new File(file));
            // workbook.write(fos);

            os = response.getOutputStream();
            workbook.write(os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @GetMapping("/datacolumns/{scenarioId}")
    public CommonResult getDataColumns(@PathVariable Long scenarioId) {
        List<DataColumnInfo> list = new ArrayList<DataColumnInfo>();
        // 1、Data
        DataColumnInfo ds = new DataColumnInfo(DataType.FIELDS.getDesc(),
                DataType.FIELDS.getName());
        List<FieldVO> listField = fieldService.listField(scenarioId);
        if (listField != null && listField.size() != 0) {
            for (FieldVO field : listField) {
                ds.addChildren(field.getLabel(), field.getFieldName(), "");
            }
            list.add(ds);
        }

        // 2、FEATURES
        ds = new DataColumnInfo(DataType.FEATURES.getDesc(),
                DataType.FEATURES.getName());
        List<FeatureVO> listFeature = featureService.listFeature(scenarioId);
        if (listFeature != null && !listFeature.isEmpty()) {
            for (FeatureVO feature : listFeature) {
                ds.addChildren(feature.getLabel(), feature.getFeatureName(), "");
            }
            list.add(ds);
        }

        // 3、STRATEGY
        ds = new DataColumnInfo(DataType.STRATEGIES.getDesc(),
                DataType.STRATEGIES.getName());
        List<StrategyVO> listStrategy = strategyService
                .listStrategy(scenarioId);
        if (listStrategy != null && listStrategy.size() != 0) {
            for (StrategyVO strategy : listStrategy) {
                ds.addChildren(strategy.getLabel(),
                        strategy.getStrategyName(), "");
            }
            list.add(ds);
        }

        // 4、 RULE
        ds = new DataColumnInfo(DataType.RULES.getDesc(),
                DataType.RULES.getName());
        List<RuleVO> listRule = ruleService.listRuleByScenarioId(scenarioId);
        if (listRule != null && listRule.size() != 0) {
            for (RuleVO rule : listRule) {
                ds.addChildren(rule.getLabel(), rule.getName(), "");
            }
            list.add(ds);
        }

        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", list);
        return result;

    }

}
