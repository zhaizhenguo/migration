package com.oscar.migration.service;

import com.oscar.migration.entity.SysMenu;
import com.oscar.migration.vo.Result;

import java.util.List;

public interface SysMenuService {

    /**
     * @description: 查询菜单树 用户名为空或admin用户时查询所有
     * @author zzg
     * @date: 2020/12/22 15:26
     * @param: [userName]
     * @return: com.oscar.migration.vo.Result
     */
    Result findTree(Long userId);

    /**
     * @description: 根据用户ID查询菜单列表
     * @author zzg
     * @date: 2020/12/21 4:52
     * @param: [userId]
     * @return: java.util.List<com.oscar.migration.entity.SysMenu>
     */
    List<SysMenu> findMenuByUserId(Long userId);

    /**
     * @description: 根据角色ID查询菜单列表
     * @author zzg
     * @date: 2020/12/21 4:52
     * @param: [roleId]
     * @return: java.util.List<com.oscar.migration.entity.SysMenu>
     */
    List<SysMenu> findMenuByRoleId(Long roleId);
}
