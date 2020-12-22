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

    @PostMapping(value = "/findTree")
    public Result findTree(@RequestParam Long userId) {
        return menuService.findTree(userId);
    }

    @PostMapping(value = "/findMenuByUserId")
    public Result findMenuByUserId(@RequestParam Long userId) {
        return Result.ok(menuService.findMenuByUserId(userId));
    }
}
