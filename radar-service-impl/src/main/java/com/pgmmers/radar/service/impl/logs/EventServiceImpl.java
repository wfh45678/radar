package com.pgmmers.radar.service.impl.logs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.pgmmers.radar.dal.bean.EventQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.TermQuery;
import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.service.logs.EventService;
import com.pgmmers.radar.service.search.SearchEngineService;
import com.pgmmers.radar.util.DateUtils;
import com.pgmmers.radar.vo.common.FieldValueVO;
import com.pgmmers.radar.vo.model.ModelVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class EventServiceImpl implements EventService {

    public static Logger logger = LoggerFactory
            .getLogger(EventServiceImpl.class);
    @Autowired
    private SearchEngineService searchService;

    @Autowired
    private ModelDal modelDal;

    @Override
    public List<Object> query(EventQuery query) throws IOException {
        List<Object> list = new ArrayList<>();
        ModelVO model = modelDal.getModelById(query.getModelId());
        String entityName = model.getEntityName();
        String dateField = model.getReferenceDate();
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("entityId", query.getEntityId());
        queryMap.put("entityName", entityName);

        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("refDate", dateField);
        Calendar beginTime = null;
        Calendar endTime = null;
        try {
            beginTime = DateUtils.parse(query.getBeginTime(),
                    "yyyy-MM-dd HH:mm:ss");
            endTime = DateUtils
                    .parse(query.getEndTime(), "yyyy-MM-dd HH:mm:ss");
            filterMap.put("beginTime", beginTime.getTimeInMillis());
            filterMap.put("endTime", endTime.getTimeInMillis());
        } catch (Exception e) {
            logger.error("error time format:{},{}", query.getBeginTime(),
                    query.getEndTime());
            filterMap = null;
        }

        SearchHits hitsRet = searchService.search(
                model.getGuid().toLowerCase(), queryMap, filterMap,
                (query.getPageNo() - 1) * query.getPageSize(),
                query.getPageSize());
        SearchHit[] hits = hitsRet.getHits();
        for (SearchHit hit : hits) {
            String info = hit.getSourceRef().utf8ToString();
            list.add(JSONObject.parse(info));
            list.add(info);
        }
        return list;
    }

    @Override
    public PageResult<Object> query(TermQuery term) {
        List<Object> list = new ArrayList<>();
        ModelVO model = modelDal.getModelById(term.getModelId());
        String dateField = model.getReferenceDate();
        Calendar beginTime = null;
        Calendar endTime = null;
        try {
            beginTime = DateUtils.parse(term.getBeginTime(),
                    "yyyy-MM-dd HH:mm:ss");
            endTime = DateUtils.parse(term.getEndTime(), "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            logger.error("error time format:{},{}", term.getBeginTime(),
                    term.getEndTime());
            return null;
        }
        QueryBuilder query = null;
        String fieldName = term.getFieldName();
        if (!StringUtils.isEmpty(fieldName)) {
            query = QueryBuilders.termQuery(term.getFieldName(),
                    term.getFieldValue());
        }
        RangeQueryBuilder filter = QueryBuilders.rangeQuery("fields."+dateField)
                .from(beginTime.getTimeInMillis())
                .to(endTime.getTimeInMillis());
        SearchHits hitsRet;
        SearchHit[] hits;
        PageResult<Object> pageResult = null;
        try {
            hitsRet = searchService.search(model.getGuid().toLowerCase(),
                    query, filter,
                    (term.getPageNo() - 1) * term.getPageSize(),
                    term.getPageSize());

            hits = hitsRet.getHits();
            for (SearchHit hit : hits) {
                //String info = hit.sourceRef().toUtf8();
                String info = hit.getSourceRef().utf8ToString();
                list.add(JSONObject.parse(info));
            }
            pageResult = new PageResult<>(term.getPageNo(),
                    term.getPageSize(), (int) hitsRet.getTotalHits().value, list);
        } catch (Exception e) {
            logger.error("", e);
        }

        return pageResult;
    }

    @Override
    public Workbook createExcel(List<Object> records,
            List<String> keyList4Field, List<String> titleList4Field,
            List<Object> keyList4Item, List<String> titleList4Item,
            List<Object> keyList4Act, List<String> titleList4Act, List<Object> keyList4Rule, List<String> titleList4Rule) {
        // 创建excel工作簿
        // Workbook wb = new XSSFWorkbook();
        // keep 1k rows in memory,
        // exceeding rows will be flushed
        // to disk
        Workbook wb = new SXSSFWorkbook(1000);

        // 创建第一个sheet（页），并命名
        Sheet sheet = wb.createSheet("event");
        // 手动设置列宽。第一个参数表示要为第几列设；，第二个参数表示列的宽度，n为列高的像素数。
        for (int i = 0; i < keyList4Field.size(); i++) {
            sheet.setColumnWidth((short) i, (short) (35.7 * 150));
        }

        for (int i = 0; i < keyList4Item.size(); i++) {
            sheet.setColumnWidth((short) i, (short) (35.7 * 150));
        }

        // 创建第一行
        Row row = sheet.createRow((short) 0);

        // 创建两种单元格格式
        CellStyle cs = wb.createCellStyle();
        CellStyle cs2 = wb.createCellStyle();

        // 创建两种字体
        Font f = wb.createFont();
        Font f2 = wb.createFont();

        // 创建第一种字体样式（用于列名）
        f.setFontHeightInPoints((short) 10);
        f.setColor(IndexedColors.BLACK.getIndex());
//        f.setBoldweight(Font.BOLDWEIGHT_BOLD);

        // 创建第二种字体样式（用于值）
        f2.setFontHeightInPoints((short) 10);
        f2.setColor(IndexedColors.BLACK.getIndex());

        // Font f3=wb.createFont();
        // f3.setFontHeightInPoints((short) 10);
        // f3.setColor(IndexedColors.RED.getIndex());

        // 设置第一种单元格的样式（用于列名）
        cs.setFont(f);
//        cs.setBorderLeft(CellStyle.BORDER_THIN);
//        cs.setBorderRight(CellStyle.BORDER_THIN);
//        cs.setBorderTop(CellStyle.BORDER_THIN);
//        cs.setBorderBottom(CellStyle.BORDER_THIN);
//        cs.setAlignment(CellStyle.ALIGN_CENTER);

        // 设置第二种单元格的样式（用于值）
        cs2.setFont(f2);
//        cs2.setBorderLeft(CellStyle.BORDER_THIN);
//        cs2.setBorderRight(CellStyle.BORDER_THIN);
//        cs2.setBorderTop(CellStyle.BORDER_THIN);
//        cs2.setBorderBottom(CellStyle.BORDER_THIN);
//        cs2.setAlignment(CellStyle.ALIGN_CENTER);
        // 设置列名
        for (int i = 0; i < titleList4Field.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(titleList4Field.get(i));
            cell.setCellStyle(cs);
        }

        for (int i = 0; i < titleList4Item.size(); i++) {
            Cell cell = row.createCell(i + titleList4Field.size());
            cell.setCellValue(titleList4Item.get(i));
            cell.setCellStyle(cs);
        }
        int startInd = titleList4Field.size() + titleList4Item.size();
        for (int i = 0; i < titleList4Act.size(); i++) {
            Cell cell = row.createCell(i + startInd);
            cell.setCellValue(titleList4Act.get(i));
            cell.setCellStyle(cs);
        }
        startInd = titleList4Field.size() + titleList4Item.size() + titleList4Act.size();
        for (int i = 0; i < titleList4Rule.size(); i++) {
            Cell cell = row.createCell(i + startInd);
            cell.setCellValue(titleList4Rule.get(i));
            cell.setCellStyle(cs);
        }

        JSONObject record;
        // 设置每行每列的值
        for (int i = 0; i < records.size(); i++) {
            // Row 行,Cell 方格 , Row 和 Cell 都是从0开始计数的
            // 创建一行，在页sheet上
            Row row1 = sheet.createRow(i + 1);

            record = JSONObject.parseObject(records.get(i).toString());
            
            int cellCount=0;

            // 在row行上创建一个方格
            for (int j = 0; j < keyList4Field.size(); j++) {
                JSONObject fieldJson = record.getJSONObject("fields");
                Cell cell = row1.createCell(cellCount);
                cellCount++;
                cell.setCellValue(fieldJson.get(keyList4Field.get(j)) == null ? " "
                        : fieldJson.get(keyList4Field.get(j)).toString());
                cell.setCellStyle(cs2);
            }

            for (int k = 0; k < keyList4Item.size(); k++) {
                JSONObject preitemsJson = record.getJSONObject("preItems");
                Cell cell = row1.createCell(cellCount);
                cellCount++;
                Object key = keyList4Item.get(k);
                if (key instanceof String) {
                    cell.setCellValue(preitemsJson.get(keyList4Item.get(k)) == null ? " "
                            : preitemsJson.get(keyList4Item.get(k)).toString());
                } else if (key instanceof FieldValueVO) {
                    FieldValueVO fvalue = (FieldValueVO) key;
                    JSONObject json = preitemsJson.getJSONObject(fvalue
                            .getKey());
                    String value = json.get(fvalue.getValue()).toString();
                    cell.setCellValue(value == null ? " " : value);
                }

                cell.setCellStyle(cs2);
            }

            for (int j = 0; j < keyList4Act.size(); j++) {
                JSONObject actJson = record.getJSONObject("activations");
                FieldValueVO fvalue = (FieldValueVO) keyList4Act.get(j);
                JSONObject json = actJson.getJSONObject(fvalue.getKey());
                Cell cell = row1.createCell(cellCount);
                cellCount++;
                String value = json.get(fvalue.getValue()).toString();
                cell.setCellValue(value == null ? " " : value);
                cell.setCellStyle(cs2);
            }
            
            for (int j = 0; j < keyList4Rule.size(); j++) {
                JSONObject hitJson = record.getJSONObject("hitsDetail");
                FieldValueVO fvalue = (FieldValueVO) keyList4Rule.get(j);
                JSONObject actiJson = hitJson.getJSONObject(fvalue.getKey());
                JSONObject detailJson = actiJson.getJSONObject(fvalue.getField());
                Cell cell = row1.createCell(cellCount);
                cellCount++;
               
                if(detailJson==null){
                	cell.setCellValue("");
                }
                else{
                	String value = detailJson.get(fvalue.getValue()).toString();
                	cell.setCellValue(value == null ? " " : value);
                }
                cell.setCellStyle(cs2);
            }

        }
        return wb;
    }

}
