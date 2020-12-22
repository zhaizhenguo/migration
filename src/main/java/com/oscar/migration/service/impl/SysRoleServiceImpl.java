package com.oscar.migration.service.impl;

import com.oscar.migration.constants.SysConstants;
import com.oscar.migration.dao.SysMenuRepository;
import com.oscar.migration.dao.SysRoleMenuRepository;
import com.oscar.migration.dao.SysRoleRepository;
import com.oscar.migration.entity.SysMenu;
import com.oscar.migration.entity.SysRole;
import com.oscar.migration.entity.SysRoleMenu;
import com.oscar.migration.service.SysRoleService;
import com.oscar.migration.constants.ResponseCode;
import com.oscar.migration.vo.PageRequest;
import com.oscar.migration.vo.PageResult;
import com.oscar.migration.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author zzg
 * @description: 用户service实现类
 */
@Service
@Transactional
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    SysRoleRepository sysRoleRepository;

    @Autowired
    SysRoleMenuRepository sysRoleMenuRepository;

    @Autowired
    SysMenuRepository sysMenuRepository;

    @Override
    public List<SysMenu> findMenusByRoleId(Long roleId) {
        return sysMenuRepository.findMenuByRoleId(roleId);
    }

    @Override
    public Result save(SysRole sysRole) {
        SysRole role = new SysRole();
        role.setName(sysRole.getName());
        role.setDelFlag(0L);
        List<SysRole> checkRole = sysRoleRepository.findAll(Example.of(role));
        if (!checkRole.isEmpty()) {
            return Result.error("角色名已存在");
        }
        Date date = new Date();
        sysRole.setCreateTime(date);
        sysRole.setLaseUpdateTime(date);
        SysRole revRole = sysRoleRepository.save(sysRole);
        /**保存角色菜单关系*/
        Result result = saveRoleOrRoleMenus(revRole.getId(), sysRole.getCreateBy(), sysRole.getRoleMenus());
        if (result.getCode() != 0) {
            return Result.error("保存菜单失败," + result.getMsg());
        }
        return Result.ok();
    }

    @Override
    public Result delete(SysRole sysRole) {
        if (sysRole == null || sysRole.getId() == null) {
            return Result.error(ResponseCode.PARAM_ERROR.code, ResponseCode.PARAM_ERROR.msg);
        }
        Optional<SysRole> op = sysRoleRepository.findById(sysRole.getId());
        if (op.isPresent()) {
            SysRole echoRole = op.get();
            if (SysConstants.ADMIN_NAME.equals(echoRole.getName())) {
                return Result.error("管理员角色，不允许删除！");
            }
            echoRole.setLaseUpdateTime(new Date());
            echoRole.setLaseUpdateBy(sysRole.getLaseUpdateBy());
            echoRole.setDelFlag(1L);
            sysRoleRepository.save(echoRole);
            /**同步删除角色菜单数据*/
            deleteRoleMenus(echoRole.getId());
            return Result.ok();
        } else {
            return Result.error("角色不存在");
        }
    }

    @Override
    public Result delete(List records) {
        return Result.ok();
    }

    @Override
    public Result update(SysRole sysRole) {
        if (sysRole == null || sysRole.getId() == null) {
            return Result.error(ResponseCode.PARAM_ERROR.code, ResponseCode.PARAM_ERROR.msg);
        }
        Optional<SysRole> op = sysRoleRepository.findById(sysRole.getId());
        if (op.isPresent()) {
            SysRole echoRole = op.get();
            if (SysConstants.ADMIN_NAME.equals(echoRole.getName())) {
                return Result.error("管理员角色，不允许修改！");
            }
            sysRole.setLaseUpdateTime(new Date());
            sysRoleRepository.save(sysRole);
            /**更新角色菜单关系*/
            Result result = saveRoleOrRoleMenus(echoRole.getId(), sysRole.getLaseUpdateBy(), sysRole.getRoleMenus());
            if (result.getCode() != 0) {
                return Result.error("更新菜单失败," + result.getMsg());
            }
            return Result.ok();
        } else {
            return Result.error("角色不存在");
        }
    }

    @Override
    public Result findById(Long id) {
        if (id == null) {
            return Result.error(ResponseCode.PARAM_ERROR.code, ResponseCode.PARAM_ERROR.msg);
        }
        Optional<SysRole> op = sysRoleRepository.findById(id);
        if (op.isPresent()) {
            return Result.ok(op.get());
        }
        return Result.error("查询失败");
    }

    @Override
    public PageResult findPage(PageRequest pagerequest) {
        return findPageCommon(pagerequest, null);
    }

    @Override
    public PageResult findPageByName(PageRequest pagerequest, String name) {
        return findPageCommon(pagerequest, name);
    }

    /**
     * @description: 查询公共方法
     * @author zzg
     * @date: 2020/12/21 4:43
     * @param: [pagerequest, name]
     * @return: com.oscar.migration.vo.PageResult
     */
    PageResult findPageCommon(PageRequest pagerequest, String name) {
        PageResult res = new PageResult();
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        SysRole sysRole = new SysRole();
        sysRole.setDelFlag(0L);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("delFlag", ExampleMatcher.GenericPropertyMatchers.contains());
        if (StringUtils.isNotBlank(name)) {
            sysRole.setName(name);
            matcher.withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains());
        }
        Example<SysRole> example = Example.of(sysRole, matcher);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pagerequest.getPageNum(), pagerequest.getPageSize(), sort);
        Page<SysRole> page = sysRoleRepository.findAll(example, pageable);
        res.setContent(page.getContent());
        res.setTotalSize(page.getTotalElements());
        res.setTotalPages(page.getTotalPages());
        return res;
    }

    /**
     * @description: 保存或更新角色菜单对应关系
     * @author zzg
     * @date: 2020/12/22 10:34
     * @param: [roleId, createBy, menuIds]
     * @return: com.oscar.migration.vo.Result
     */
    public Result saveRoleOrRoleMenus(Long roleId, String createBy, List<Long> menuIds) {
        if (roleId == null || menuIds.isEmpty()) {
            return Result.error("参数为空");
        }
        Optional<SysRole> role = sysRoleRepository.findById(roleId);
        if (role.isPresent()) {
            if (SysConstants.ADMIN_NAME.equals(role.get().getName())) {
                return Result.error("管理员拥有所有菜单权限，不允许修改！");
            }
            deleteRoleMenus(roleId);
            List<SysRoleMenu> list = new ArrayList<>();
            Date date = new Date();
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenu.setCreateBy(createBy);
                roleMenu.setLaseUpdateBy(createBy);
                roleMenu.setCreateTime(date);
                roleMenu.setLaseUpdateTime(date);
                list.add(roleMenu);
            }
            sysRoleMenuRepository.saveAll(list);
            return Result.ok();
        }
        return Result.error("角色不存在");
    }

    /**
     * @description: 根据角色ID删除角色菜单对应关系
     * @author zzg
     * @date: 2020/12/22 17:06
     * @param: [roleId]
     * @return: void
     */
    void deleteRoleMenus(Long roleId) {
        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(roleId);
        List<SysRoleMenu> delAll = sysRoleMenuRepository.findAll(Example.of(rm));
        sysRoleMenuRepository.deleteAll(delAll);
        sysRoleMenuRepository.flush();
    }

}
