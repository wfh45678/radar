package com.pgmmers.radar.dal.bean;

import java.util.List;

import com.github.pagehelper.Page;

public class PageResult<T> {

	private int rowCount = 0; // 记录总数
	private int pageCount = 1; // 分页总数
	private int pageSize = 10; // 每页记录数
	private int pageNum = 1; // 当前页数
	private int startIndex = 1; // 起始记录数
	private int endIndex = 1; // 结束记录数
	private List<T> list;// 记录列表

	public PageResult(int pageNum, int pageSize, int rowCount) {
		preProcessParams(pageNum, pageSize);
		this.rowCount = rowCount;
		countPage();
	}

	public PageResult(int pageNum, int pageSize, int rowCount, List<T> list) {
		this(pageNum, pageSize, rowCount);
		this.list = list;
	}

	public PageResult(Page<T> page) {
		this(page.getPageNum(), page.getPageSize(), (int) page.getTotal(), page.getResult());
	}

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

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

}
