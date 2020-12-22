package com.oscar.migration.service.impl;


import com.oscar.migration.constants.SysConstants;
import com.oscar.migration.dao.SysUserRepository;
import com.oscar.migration.dao.SysUserRoleRepository;
import com.oscar.migration.entity.SysUser;
import com.oscar.migration.entity.SysUserRole;
import com.oscar.migration.constants.ResponseCode;
import com.oscar.migration.vo.PageRequest;
import com.oscar.migration.vo.PageResult;
import com.oscar.migration.service.SysUserService;
import com.oscar.migration.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author zzg
 * @description: 用户service实现类
 */
@Service
@Transactional
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    SysUserRepository sysUserRepository;

    @Autowired
    SysUserRoleRepository sysUserRoleRepository;

    @Override
    public Result findRolesByUserId(Long userId) {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        List<SysUserRole> findAll = sysUserRoleRepository.findAll(Example.of(userRole));
        return Result.ok(findAll);
    }

    /**
     * @description: 保存或修改用户角色对应关系
     * @author zzg
     * @date: 2020/12/22 16:24
     * @param: [userId 用户ID, createBy 创建人, roleIds 角色集合]
     * @return: com.oscar.migration.vo.Result
     */
    public Result saveOrUpdateUserRoles(Long userId, String createBy, List<Long> roleIds) {
        if (userId == null || roleIds.isEmpty()) {
            return Result.error("参数为空");
        }
        Optional<SysUser> user = sysUserRepository.findById(userId);
        if (user.isPresent()) {
            String userName = user.get().getName();
            if (SysConstants.ADMIN.equalsIgnoreCase(userName)) {
                return Result.error("管理员不允许修改！");
            }
            deleteUserRoles(userId);
            List<SysUserRole> userRoles = new ArrayList<>();
            Date date = new Date();
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateBy(createBy);
                userRole.setCreateTime(date);
                userRole.setLaseUpdateBy(createBy);
                userRole.setLaseUpdateTime(date);
                userRoles.add(userRole);
            }
            sysUserRoleRepository.saveAll(userRoles);
            return Result.ok();
        }
        return Result.error("用户不存在");
    }

    /**
     * @description: 根据用户ID删除用户角色表内容
     * @author zzg
     * @date: 2020/12/22 16:49
     * @param: [userId]
     * @return: void
     */
    void deleteUserRoles(Long userId) {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        List<SysUserRole> delAll = sysUserRoleRepository.findAll(Example.of(ur));
        sysUserRoleRepository.deleteAll(delAll);
        sysUserRoleRepository.flush();
    }

    @Override
    public Result save(SysUser sysUser) {
        SysUser user = new SysUser();
        user.setName(sysUser.getName());
        sysUser.setDelFlag(0L);
        List<SysUser> checkUser = sysUserRepository.findAll(Example.of(user));
        if (!checkUser.isEmpty()) {
            return Result.error("用户名已存在");
        }
        Date date = new Date();
        sysUser.setCreateTime(date);
        sysUser.setLaseUpdateTime(date);
        SysUser revUser = sysUserRepository.save(sysUser);
        sysUserRepository.flush();
        /**保存用户角色关系*/
        Result result = saveOrUpdateUserRoles(revUser.getId(), sysUser.getCreateBy(), sysUser.getUserRoles());
        if (result.getCode() != 0) {
            return Result.error("保存角色失败," + result.getMsg());
        }
        return Result.ok();
    }

    @Override
    public Result delete(SysUser sysUser) {
        if (sysUser == null || sysUser.getId() == null) {
            return Result.error(ResponseCode.PARAM_ERROR.code, ResponseCode.PARAM_ERROR.msg);
        }
        Optional<SysUser> opUser = sysUserRepository.findById(sysUser.getId());
        if (opUser.isPresent()) {
            SysUser user = opUser.get();
            if (SysConstants.ADMIN.equalsIgnoreCase(user.getName())) {
                Result.error("管理员用户不允许删除!");
            }
            user.setLaseUpdateTime(new Date());
            user.setLaseUpdateBy(sysUser.getLaseUpdateBy());
            user.setDelFlag(1L);
            sysUserRepository.save(user);
            /**同步删除用户角色关联信息*/
            deleteUserRoles(user.getId());
            return Result.ok();
        }
        return Result.error("用户不存在");
    }

    @Override
    public Result delete(List records) {
        return Result.ok();
    }

    @Override
    public Result update(SysUser sysUser) {
        if (sysUser == null || sysUser.getId() == null) {
            return Result.error(ResponseCode.PARAM_ERROR.code, ResponseCode.PARAM_ERROR.msg);
        }
        Optional<SysUser> opUser = sysUserRepository.findById(sysUser.getId());
        if (opUser.isPresent()) {
            SysUser user = opUser.get();
            if (SysConstants.ADMIN.equalsIgnoreCase(user.getName())) {
                Result.error("管理员用户不允许修改!");
            }
            sysUser.setLaseUpdateTime(new Date());
            sysUserRepository.save(sysUser);
            Result result = saveOrUpdateUserRoles(user.getId(), sysUser.getCreateBy(), sysUser.getUserRoles());
            if (result.getCode() != 0) {
                return Result.error("保存角色失败," + result.getMsg());
            }
            return Result.ok();
        }
        return Result.error("用户不存在");
    }

    @Override
    public Result findById(Long id) {
        if (id == null) {
            return Result.error(ResponseCode.PARAM_ERROR.code, ResponseCode.PARAM_ERROR.msg);
        }
        Optional<SysUser> op = sysUserRepository.findById(id);
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

    PageResult findPageCommon(PageRequest pagerequest, String name) {
        PageResult res = new PageResult();
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        SysUser sysUser = new SysUser();
        sysUser.setDelFlag(0L);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("delFlag", ExampleMatcher.GenericPropertyMatchers.contains());
        if (StringUtils.isNotBlank(name)) {
            sysUser.setName(name);
            matcher.withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains());
        }
        Example<SysUser> example = Example.of(sysUser, matcher);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pagerequest.getPageNum(), pagerequest.getPageSize(), sort);
        Page<SysUser> page = sysUserRepository.findAll(example, pageable);
        res.setContent(page.getContent());
        res.setTotalSize(page.getTotalElements());
        res.setTotalPages(page.getTotalPages());
        return res;
    }

}
