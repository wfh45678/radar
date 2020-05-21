package com.pgmmers.radar.vo.common;

import java.util.List;


/**
 * <p>PageVO class.</p>
 *
 * @author zhang
 */
public class PageVO<T> {

    private int rowCount = 0; // 记录总数
    private int pageCount = 1; // 分页总数
    private int pageSize = 10; // 每页记录数
    private int pageNum = 1; // 当前页数
    private int startIndex = 1; // 起始记录数
    private int endIndex = 1; // 结束记录数
    private List<T> list;// 记录列表
    
    /**
     * 初始化分页对象
     *
     * @param pageNum a int.
     * @param pageSize a int.
     * @param rowCount a int.
     */
    public PageVO(int pageNum, int pageSize,int rowCount) {
        preProcessParams(pageNum, pageSize);
        this.rowCount = rowCount;
        countPage();
    }

    /**
     * 预处理SQL语句和页面参数
     */
    private void preProcessParams(int pageNum, int pageSize) {
        if (pageNum > 0) {
            this.pageNum = pageNum;
        }
        if (pageSize > 0) {
            this.pageSize = pageSize;
        }
        if (pageSize > 1000) {
            this.pageSize = 1000;
        }
        
    }

    /**
     * 计算分页数及起止记录
     */
    private void countPage() {
        // 计算分页总数
        if ((rowCount % pageSize) == 0) {
            pageCount = rowCount / pageSize;
        } else {
            pageCount = rowCount / pageSize + 1;
        }
        if (pageCount == 0) {
            pageCount = 1;
        }
        // 判断pageNum是否过界
        if (pageNum > pageCount && rowCount != 0) {
            pageNum = pageCount;
        }
        // 计算起止记录
        startIndex = (pageNum - 1) * pageSize + 1;
        endIndex = (pageNum) * pageSize;
    }

    /**
     * 获得对象列表
     *
     * @return a {@link java.util.List} object.
     */
    public List<T> getList() {
        return list;
    }

    /* 获得起始记录数 */
    /**
     * <p>Getter for the field <code>startIndex</code>.</p>
     *
     * @return a int.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * <p>getStartIndexInteger.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getStartIndexInteger() {
        return startIndex;
    }

    /* 获得结束记录数 */
    /**
     * <p>Getter for the field <code>endIndex</code>.</p>
     *
     * @return a int.
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * <p>getEndIndexInteger.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getEndIndexInteger() {
        return endIndex;
    }

    /* 获得分页其它信息 */
    /**
     * <p>Getter for the field <code>pageCount</code>.</p>
     *
     * @return a int.
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * <p>Getter for the field <code>pageNum</code>.</p>
     *
     * @return a int.
     */
    public int getPageNum() {
        return pageNum;
    }

    /**
     * <p>Getter for the field <code>pageSize</code>.</p>
     *
     * @return a int.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * <p>Getter for the field <code>rowCount</code>.</p>
     *
     * @return a int.
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * <p>Setter for the field <code>list</code>.</p>
     *
     * @param list a {@link java.util.List} object.
     */
    public void setList(List<T> list) {
        this.list = list;
    }
    
    

}
