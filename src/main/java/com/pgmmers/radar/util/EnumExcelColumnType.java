package com.pgmmers.radar.util;

/**
 * Excel字段类型
 * @author xushuai
 */
public enum EnumExcelColumnType {
    ColumnType_Double(0),
    ColumnType_Date(1),
    ColumnType_Calendar(2),
    ColumnType_String(3),
    ColumnType__BOOLEAN(4),
    ColumnType_ERROR(5);
    private int RowId;
    private EnumExcelColumnType(int id) {
        RowId = id;
    }
    public int GetValue() {
        return RowId;
    }
}
