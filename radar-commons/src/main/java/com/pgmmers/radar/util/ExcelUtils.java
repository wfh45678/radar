package com.pgmmers.radar.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

/**
 * <p>ExcelUtils class.</p>
 *
 */
public class ExcelUtils {

    /**
     * 创建excel文档.
     *
     * @param list a {@link List} object.
     * @param keys a {@link String[]} object.
     * @param columnNames a {@link String[]} object.
     * @return a {@link org.apache.poi.ss.usermodel.Workbook} object.
     */
    public static Workbook createWorkBook(List<Map<String, Object>> list,String []keys,String columnNames[]) {
        // 创建excel工作簿
        // Workbook wb = new XSSFWorkbook();
        Workbook wb = new SXSSFWorkbook(1000);// keep 1k rows in memory, exceeding rows will be flushed to disk

        //for(int pageCount=0;pageCount<count/50000;pageCount++){
            // 创建第一个sheet（页），并命名
            Sheet sheet = wb.createSheet(list.get(0).get("sheetName").toString());
            // 手动设置列宽。第一个参数表示要为第几列设；，第二个参数表示列的宽度，n为列高的像素数。
            for(int i=0;i<keys.length;i++){
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
//            f.setBoldweight(Font.BOLDWEIGHT_BOLD);
    
            // 创建第二种字体样式（用于值）
            f2.setFontHeightInPoints((short) 10);
            f2.setColor(IndexedColors.BLACK.getIndex());
    
    //      Font f3=wb.createFont();
    //      f3.setFontHeightInPoints((short) 10);
    //      f3.setColor(IndexedColors.RED.getIndex());
    
            // 设置第一种单元格的样式（用于列名）
            cs.setFont(f);
//            cs.setBorderLeft(CellStyle.BORDER_THIN);
//            cs.setBorderRight(CellStyle.BORDER_THIN);
//            cs.setBorderTop(CellStyle.BORDER_THIN);
//            cs.setBorderBottom(CellStyle.BORDER_THIN);
//            cs.setAlignment(CellStyle.ALIGN_CENTER);
    
            // 设置第二种单元格的样式（用于值）
            cs2.setFont(f2);
//            cs2.setBorderLeft(CellStyle.BORDER_THIN);
//            cs2.setBorderRight(CellStyle.BORDER_THIN);
//            cs2.setBorderTop(CellStyle.BORDER_THIN);
//            cs2.setBorderBottom(CellStyle.BORDER_THIN);
//            cs2.setAlignment(CellStyle.ALIGN_CENTER);
            //设置列名
            for(int i=0;i<columnNames.length;i++){
                Cell cell = row.createCell(i);
                cell.setCellValue(columnNames[i]);
                cell.setCellStyle(cs);
            }
            //设置每行每列的值
            for (int i = 1; i < list.size(); i++) {
                // Row 行,Cell 方格 , Row 和 Cell 都是从0开始计数的
                // 创建一行，在页sheet上
                Row row1 = sheet.createRow(i);
                // 在row行上创建一个方格
                for(int j=0;j<keys.length;j++){
                    Cell cell = row1.createCell(j);
                    cell.setCellValue(list.get(i).get(keys[j]) == null?" ": list.get(i).get(keys[j]).toString());
                    cell.setCellStyle(cs2);
                }
            }
        //}
        return wb;
    }
    
    
    public static void readExcel(String filePath) throws Exception {
        InputStream is = new FileInputStream(new File(filePath));
        XSSFWorkbook wb = new XSSFWorkbook(is);
        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFRow row = null;
        int rowNum = 0;
        while ((row = sheet.getRow(++rowNum)) != null && row.getCell(0) != null) {
            
        }
    }

    /**
     * 从 row 的第 index 列，获取字符串值
     *
     * @param row   目标行
     * @param index 目标列
     * @return 字符串值
     * @author xushuai
     */
    public static String getString(Row row, int index) {
        return getString(row, index, null);
    }

    /**
     * 从 row 的第 index 列，获取字符串值
     *
     * @param row          目标行
     * @param index        目标列
     * @param numberFormat 对原值为数字时的，数字格式化格式
     * @return 字符串值
     * @author xushuai
     */
    public static String getString(Row row, int index, String numberFormat) {
        Cell cell = row.getCell(index);

        if (cell == null) return null;

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                double val = cell.getNumericCellValue();
                if (numberFormat == null)
                    return String.valueOf(val);
                NumberFormat formatter = new DecimalFormat(numberFormat);
                return formatter.format(val);
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_FORMULA:
                return cell.getStringCellValue();
            default:
                return null;
        }
    }
}
