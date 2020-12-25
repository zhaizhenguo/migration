package com.oscar.migration.dao;

import com.oscar.migration.entity.SysMenu;
import com.oscar.migration.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @description: 用戶Repository
 * @author zzg
 * @date 2020/12/21 4:03
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long>
{

    @Query(value = "SELECT * FROM SYS_USER WHERE NAME = ?1", nativeQuery = true)
    SysUser findUserByUserName(String userName);

    @Query(value = "SELECT GROUP_CONCAT(r.NAME)   FROM SYS_USER_ROLE ur ,SYS_ROLE r WHERE ur.USER_ID = ?1 AND ur.ROLE_ID = r.ID", nativeQuery = true)
    String findUserRoleByUserId(Long id);
}
