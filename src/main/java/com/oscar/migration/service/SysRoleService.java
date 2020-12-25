package com.oscar.migration.service;

import com.oscar.migration.entity.SysRole;
import com.oscar.migration.vo.Result;

public interface SysRoleService extends BaseCurdService<SysRole> {


    /**
     * @description: 查询所有角色
     * @author zzg
     * @date: 2020/12/25 9:40
     * @return: com.oscar.migration.vo.Result
     */
    Result findAll();
}
