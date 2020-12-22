package com.oscar.migration.dao;

import com.oscar.migration.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @description: 用户角色关系Repository
 * @author zzg
 * @date 2020/12/21 4:05
 */
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long>
{
}
