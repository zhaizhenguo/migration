package com.oscar.migration.dao;

import com.oscar.migration.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @description: 用戶Repository
 * @author zzg
 * @date 2020/12/21 4:03
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long>
{
}
