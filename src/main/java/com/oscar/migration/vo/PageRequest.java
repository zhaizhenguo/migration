package com.oscar.migration.vo;

import lombok.Data;

/**
 * @description: 分页请求
 * @author zzg
 * @date 2020/12/18 15:23
 */
@Data
public class PageRequest {
	/**
	 * 当前页码
	 */
	private int pageNum = 0;
	/**
	 * 每页数量
	 */
	private int pageSize = 10;
}
