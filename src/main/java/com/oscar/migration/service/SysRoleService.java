package com.oscar.migration.service;

import com.oscar.migration.entity.SysMenu;
import com.oscar.migration.entity.SysRole;
import com.oscar.migration.vo.PageRequest;
import com.oscar.migration.vo.PageResult;

import java.util.List;

public interface SysRoleService extends BaseCurdService<SysRole> {

    /**
     * @description: 根据角色名查询角色列表(模糊查询)
     * @author zzg
     * @date: 2020/12/21 4:45
     * @param: [pagerequest分页公共入参, name角色名]
     * @return: com.oscar.migration.vo.PageResult
     */
    PageResult findPageByName(PageRequest pagerequest, String name);

    /**
     * @description: 查找角色的菜单集合
     * @author zzg
     * @date: 2020/12/21 4:46
     * @param: [roleId 角色ID]
     * @return: java.util.List<com.oscar.migration.entity.SysMenu>
     */
    List<SysMenu> findMenusByRoleId(Long roleId);

}
