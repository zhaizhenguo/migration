package com.oscar.migration.controller;

import com.oscar.migration.entity.SysUser;
import com.oscar.migration.vo.LoginPassword;
import com.oscar.migration.vo.PageRequest;
import com.oscar.migration.service.SysUserService;
import com.oscar.migration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zzg
 * @description: 用户管理controller
 * @date 2020/12/18 15:59
 */
@RestController
@RequestMapping("user")
public class SysUserController {

    @Autowired
    SysUserService userService;

    @PostMapping(value = "/save")
    public Result save(@RequestBody SysUser sysUser) {
        return userService.save(sysUser);
    }

    @PostMapping(value = "/delete")
    public Result delete(@RequestBody SysUser sysUser) {
        return userService.delete(sysUser);
    }

    @PostMapping(value = "/update")
    public Result update(@RequestBody SysUser sysUser) {
        return userService.update(sysUser);
    }

    @PostMapping(value = "/updatePassword")
    public Result updatePassword(@RequestBody LoginPassword loginPassword) {
        return userService.updatePassword(loginPassword);
    }

    @PostMapping(value = "/findPage")
    public Result findPage(@RequestBody PageRequest pageRequest) {
        return Result.ok(userService.findPage(pageRequest));
    }

    @GetMapping(value = "/findRolesByUserId")
    public Result findRolesByUserId(@RequestParam(name="id") Long id) {
        return userService.findRolesByUserId(id);
    }


}
