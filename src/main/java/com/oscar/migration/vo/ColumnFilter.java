package com.oscar.migration.vo;

import lombok.Data;

/**
 * 分页查询列过滤器
 */
@Data
public class ColumnFilter {

	/**
	 * 过滤列名
	 */
	private String name;
	/**
	 * 查询的值
	 */
	private String value;

}
