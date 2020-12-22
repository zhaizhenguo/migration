package com.oscar.migration.dao;

import com.oscar.migration.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @description: 角色Repository
 * @author zzg
 * @date 2020/12/21 4:03
 */
public interface SysRoleRepository extends JpaRepository<SysRole, Long>
{
}
