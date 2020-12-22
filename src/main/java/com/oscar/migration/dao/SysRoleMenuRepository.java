package com.oscar.migration.dao;

import com.oscar.migration.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @description: 角色菜单关系Repository
 * @author zzg
 * @date 2020/12/21 4:03
 */
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Long>
{
}
