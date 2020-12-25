package com.oscar.migration.service.impl;

import com.oscar.migration.constants.SysConstants;
import com.oscar.migration.dao.SysMenuRepository;
import com.oscar.migration.dao.SysUserRepository;
import com.oscar.migration.entity.SysMenu;
import com.oscar.migration.entity.SysUser;
import com.oscar.migration.service.SysMenuService;
import com.oscar.migration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SysMenuServiceImpl implements SysMenuService {

    @Autowired
    SysMenuRepository sysMenuRepository;

    @Resource
    SysUserRepository sysUserRepository;

    @Override
    public Result findTree(Long userId) {
        /**menu结果集*/
        List<SysMenu> resMenuList = new ArrayList<>();
        /**除去顶级节点的menu集合*/
        List<SysMenu> subMenuList = new ArrayList<>();
        /**用户权限menu集合*/
        List<SysMenu> menuList = findMenuByUserId(userId);
        for (SysMenu sysMenu : menuList) {
            if (sysMenu.getParentId() == null || sysMenu.getParentId() == 0) {
                resMenuList.add(sysMenu);
            } else {
                subMenuList.add(sysMenu);
            }
        }
        resMenuList.sort((o1, o2) -> o1.getOrderNum().compareTo(o2.getOrderNum()));
        findChildren(resMenuList, subMenuList);
        return Result.ok(resMenuList);
    }

    /**
     * @description: 遍历顶级菜单 找到其子菜单
     * @author zzg
     * @date: 2020/12/22 15:27
     * @param: [superMenuList, submenuList]
     * @return: void
     */
    void findChildren(List<SysMenu> superMenuList, List<SysMenu> submenuList) {
        for (SysMenu supMenu : superMenuList) {
            List<SysMenu> childrenMenu = new ArrayList<>();
            for (SysMenu subMenu : submenuList) {
                if (subMenu.getParentId() != null && subMenu.getParentId().equals(supMenu.getId())) {
                    subMenu.setParentName(supMenu.getName());
                    childrenMenu.add(subMenu);
                }
            }
            childrenMenu.sort((o1, o2) -> o1.getOrderNum().compareTo(o2.getOrderNum()));
            supMenu.setChildren(childrenMenu);
            findChildren(childrenMenu, submenuList);
        }
    }

    @Override
    public List<SysMenu> findMenuByUserId(Long userId) {
        if (userId == null) {
            return sysMenuRepository.findAll();
        }
        Optional<SysUser> user = sysUserRepository.findById(userId);
        String userName = null;
        if (user.isPresent()) {
            userName = user.get().getName();
        }
        if ( SysConstants.ADMIN.equalsIgnoreCase(userName)) {
            return sysMenuRepository.findAll();
        }
        return sysMenuRepository.findMenuByUserId(userId);
    }

    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        return sysMenuRepository.findMenuByRoleId(roleId);
    }

}
