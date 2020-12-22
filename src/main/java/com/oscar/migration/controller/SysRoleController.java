package com.oscar.migration.controller;

import com.oscar.migration.entity.SysRole;
import com.oscar.migration.service.SysRoleService;
import com.oscar.migration.vo.PageRequest;
import com.oscar.migration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zzg
 * @description: 角色管理controller
 * @date 2020/12/22 16:55
 */
@RestController
@RequestMapping("role")
public class SysRoleController {

    @Autowired
    SysRoleService roleService;

    @PostMapping(value = "/save")
    public Result save(@RequestBody SysRole sysRole) {
        return roleService.save(sysRole);
    }

    @PostMapping(value = "/delete")
    public Result delete(@RequestBody SysRole sysRole) {
        return roleService.delete(sysRole);
    }

    @PostMapping(value = "/update")
    public Result update(@RequestBody SysRole sysRole) {
        return roleService.update(sysRole);
    }

    @PostMapping(value = "/findPage")
    public Result findPage(@RequestBody PageRequest pageRequest) {
        return Result.ok(roleService.findPage(pageRequest));
    }

    @PostMapping(value = "/findPageByName")
    public Result findPageByName(@RequestBody PageRequest pageRequest, @RequestParam String name) {
        return Result.ok(roleService.findPageByName(pageRequest, name));
    }

    @PostMapping(value = "/findMenusByRoleId")
    public Result findMenusByRoleId(@RequestParam Long roleId) {
        return Result.ok(roleService.findMenusByRoleId(roleId));
    }
}
