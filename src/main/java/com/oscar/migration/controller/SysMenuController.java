package com.oscar.migration.controller;

import com.oscar.migration.service.SysMenuService;
import com.oscar.migration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zzg
 * @description: 菜单管理controller
 * @date 2020/12/18 15:59
 */
@RestController
@RequestMapping("menu")
public class SysMenuController {

    @Autowired
    SysMenuService menuService;

    @GetMapping(value = "/findTree")
    public Result findTree(@RequestParam(name = "userId",required = false) Long userId) {
        return menuService.findTree(userId);
    }

    @GetMapping(value = "/findMenuByRoleId")
    public Result findMenuByRoleId(@RequestParam(name = "roleId") Long roleId) {
        return Result.ok(menuService.findMenuByRoleId(roleId));
    }
}
