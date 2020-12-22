package com.oscar.migration.service;

import com.oscar.migration.entity.SysUser;
import com.oscar.migration.vo.PageRequest;
import com.oscar.migration.vo.PageResult;
import com.oscar.migration.vo.Result;

public interface SysUserService extends BaseCurdService<SysUser> {

    /**
     * 根据姓名查询
     *
     * @param pagerequest
     * @param name
     * @author zzg
     * @date: 2020/12/21 4:27
     * @return: com.oscar.migration.vo.PageResult
     */
    PageResult findPageByName(PageRequest pagerequest, String name);

    /**
     * @description: 查找用户的角色集合
     * @author zzg
     * @date: 2020/12/22 17:32
     * @param: [userId]
     * @return: com.oscar.migration.vo.Result
     */
    Result findRolesByUserId(Long userId);

}
