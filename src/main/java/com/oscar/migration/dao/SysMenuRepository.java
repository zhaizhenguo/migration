package com.oscar.migration.dao;

import com.oscar.migration.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author zzg
 * @description: 菜单Repository
 * @date 2020/12/21 4:03
 */
public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {

    @Query(value = "SELECT DISTINCT M.ID, M.NAME, M.PARENT_ID, M.URL, M.TYPE, M.ICON, M.ORDER_NUM, M.CREATE_BY, M.CREATE_TIME," +
            " M.LAST_UPDATE_BY, M.LAST_UPDATE_TIME ,M.DEL_FLAG FROM SYS_MENU M,SYS_USER U,SYS_USER_ROLE UR,SYS_ROLE_MENU RM" +
            " WHERE U.ID = ?1 AND U.ID = UR.USER_ID AND UR.ROLE_ID = RM.ROLE_ID AND RM.MENU_ID = M.ID", nativeQuery = true)
    List<SysMenu> findMenuByUserId(Long userId);

    @Query(value = "SELECT DISTINCT M.ID, M.NAME, M.PARENT_ID, M.URL, M.TYPE, M.ICON, M.ORDER_NUM, M.CREATE_BY, M.CREATE_TIME," +
            " M.LAST_UPDATE_BY, M.LAST_UPDATE_TIME ,M.DEL_FLAG FROM SYS_MENU M,SYS_USER U,SYS_USER_ROLE UR,SYS_ROLE_MENU RM" +
            " WHERE RM.ROLE_ID = ?1 AND RM.MENU_ID = M.ID", nativeQuery = true)
    List<SysMenu> findMenuByRoleId(Long roleId);
}
