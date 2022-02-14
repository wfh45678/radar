/*
 * Copyright (c) 2019 WangFeiHu
 *  Radar is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *  http://license.coscl.org.cn/MulanPSL2
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 */

package com.pgmmers.radar.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pgmmers.radar.dal.bean.EventExportQuery;
import com.pgmmers.radar.dal.bean.EventQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.TermQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.service.engine.vo.DataColumnInfo;
import com.pgmmers.radar.service.enums.DataType;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.logs.EventService;
import com.pgmmers.radar.service.model.ActivationService;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.service.model.PreItemService;
import com.pgmmers.radar.service.model.RuleService;
import com.pgmmers.radar.vo.common.FieldValueVO;
import com.pgmmers.radar.vo.model.ActivationVO;
import com.pgmmers.radar.vo.model.FieldVO;
import com.pgmmers.radar.vo.model.PreItemVO;
import com.pgmmers.radar.vo.model.RuleVO;
import io.swagger.annotations.Api;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
@Api(value = "EventApi", description = "事件信息检索接口相关操作",  tags = {"事件信息检索分析API"}, hidden = true)
public class EventApiController {

    @Autowired
    private EventService eventService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private PreItemService preItemService;

    @Autowired
    private ActivationService activationService;

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
        if (term == null || term.getModelId() == null) {
            return;
        }
        EventExportQuery query = (EventExportQuery) request.getSession().getAttribute("exportQuery");

        if (query == null) {
            return;
        }
        String[] fieldIds = query.getFields();
        String[] preItems = query.getPreItems();
        String[] activations = query.getActivations();
        String[] rules = query.getRules();

        Map<String, String> fieldIdMap = new HashMap<>();
        Map<String, String> itemsIdMap = new HashMap<>();
        Map<String, String> actIdMap = new HashMap<>();
        Map<String, String> ruleIdMap = new HashMap<>();

        for (int i = 0; i < fieldIds.length; i++) {
            fieldIdMap.put(fieldIds[i], "");
        }

        for (int i = 0; i < preItems.length; i++) {
            itemsIdMap.put(preItems[i], "");
        }

        for (int i = 0; i < activations.length; i++) {
            actIdMap.put(activations[i], "");
        }

        for (int i = 0; i < rules.length; i++) {
            ruleIdMap.put(rules[i], "");
        }

        term.setPageNo(1);
        // 暂时限制最大导出1W条
        term.setPageSize(10000);

        PageResult<Object> page = eventService.query(term);
        List<Object> records = page.getList();

        List<FieldVO> fieldList = fieldService.listField(term.getModelId());
        List<PreItemVO> preitemList = preItemService.listPreItem(term
                .getModelId());
        List<ActivationVO> activationList = activationService
                .listActivation(term.getModelId());
        List<RuleVO> ruleList = ruleService
                .listRuleByModelId(term.getModelId());

        // 关联 rule id　与　activation
        for (ActivationVO act : activationList) {
            List<RuleVO> ruleVOList = ruleService.listRule(act.getId());
            for (RuleVO rule : ruleVOList) {
                if (ruleIdMap.containsKey(rule.getName())) {
                    ruleIdMap.put(rule.getName(), act.getActivationName());
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

        // preitem field
        List<Object> keyList4Item = new ArrayList<>();
        List<String> titleList4Item = new ArrayList<>();
        for (PreItemVO item : preitemList) {
            if (!itemsIdMap.containsKey(item.getDestField())) {
                continue;
            }
            PluginServiceV2 plugin = pluginManager.pluginServiceMap(item.getPlugin());
            String type = plugin.getType();
            String meta = plugin.getMeta();

            if (type != null) {
                keyList4Item.add(item.getDestField());
                titleList4Item.add(item.getLabel());
            } else {
                List<JSONObject> metaArray = JSONArray.parseArray(meta,
                        JSONObject.class);
                for (JSONObject col : metaArray) {
                    keyList4Item.add(new FieldValueVO(item.getDestField(), col
                            .getString("column")));
                    titleList4Item.add(item.getLabel() + "."
                            + col.getString("title"));
                }

            }
        }

        // activation field
        List<Object> keyList4Act = new ArrayList<>();
        List<String> titleList4Act = new ArrayList<>();
        for (ActivationVO act : activationList) {
            if (!actIdMap.containsKey(act.getActivationName())) {
                continue;
            }
            keyList4Act.add(new FieldValueVO(act.getActivationName(), "risk"));
            keyList4Act.add(new FieldValueVO(act.getActivationName(), "score"));
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

    @GetMapping("/datacolumns/{modelId}")
    public CommonResult getDataColumns(@PathVariable Long modelId) {
        List<DataColumnInfo> list = new ArrayList<DataColumnInfo>();
        // 1、Data
        DataColumnInfo ds = new DataColumnInfo(DataType.FIELDS.getDesc(),
                DataType.FIELDS.getName());
        List<FieldVO> listField = fieldService.listField(modelId);
        if (listField != null && listField.size() != 0) {
            for (FieldVO field : listField) {
                ds.addChildren(field.getLabel(), field.getFieldName(), "");
            }
            list.add(ds);
        }

        // 2、PREPARE
        ds = new DataColumnInfo(DataType.PREITEMS.getDesc(),
                DataType.PREITEMS.getName());
        List<PreItemVO> listPreItem = preItemService.listPreItem(modelId);
        if (listPreItem != null && listPreItem.size() != 0) {
            for (PreItemVO preItem : listPreItem) {
                ds.addChildren(preItem.getLabel(), preItem.getDestField(), "");
            }
            list.add(ds);
        }

        // 3、ACTIVATION
        ds = new DataColumnInfo(DataType.ACTIVATIONS.getDesc(),
                DataType.ACTIVATIONS.getName());
        List<ActivationVO> listActivation = activationService
                .listActivation(modelId);
        if (listActivation != null && listActivation.size() != 0) {
            for (ActivationVO activation : listActivation) {
                ds.addChildren(activation.getLabel(),
                        activation.getActivationName(), "");
            }
            list.add(ds);
        }

        // 4、 RULE
        ds = new DataColumnInfo(DataType.RULES.getDesc(),
                DataType.RULES.getName());
        List<RuleVO> listRule = ruleService.listRuleByModelId(modelId);
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
