package com.oscar.migration.vo;

import lombok.Data;

import java.util.List;

/**
 * @description: 分页返回结果
 * @author zzg
 * @date 2020/12/18 14:32
 */
@Data
public class PageResult {
	/**
	 * 当前页码
	 */
	private int pageNum;
	/**
	 * 每页数量
	 */
	private int pageSize;
	/**
	 * 记录总数
	 */
	private long totalSize;
	/**
	 * 页码总数
	 */
	private int totalPages;
	/**
	 * 分页数据
	 */
	private List<?> content;

}
