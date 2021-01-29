package com.oscar.migration.controller;

import com.alibaba.fastjson.JSON;
import com.oscar.entity.*;
import com.oscar.migration.entity.UserResource;
import com.oscar.migration.service.DataMigrationService;
import com.oscar.migration.util.UserResourceUtils;
import com.oscar.migration.vo.ConnectionInfo;
import com.oscar.migration.vo.MigrationConfigInfo;
import com.oscar.migration.vo.Result;
import com.oscar.ui.common.controller.PageController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author zzg
 * @description: 数据迁移控制层
 * @date 2021/1/12 13:27
 */
@RestController
@Slf4j
@RequestMapping("dataMigration")
public class DataMigrationController {

    @Autowired
    DataMigrationService migrationService;

    /**
     * @description: 测试连接
     * @author zzg
     * @date 2021/1/12 13:51
     */
    @PostMapping(value = "/testConnection")
    public Result testConnection(@RequestBody ConnectionInfo connectionInfo, HttpServletRequest request) {
        UserResource userResource = UserResourceUtils.getUserResource(request);
        PageController pageController = UserResourceUtils.getPageController(request);
        if (userResource == null || pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        return migrationService.testConnection(userResource, pageController, connectionInfo);
    }

    /**
     * @description: 迁移参数保存接口
     * @author zzg
     * @date 2021/1/12 13:51
     */
    @PostMapping(value = "/saveConfig")
    public Result saveConfig(@RequestBody ConfigInfo configInfo, HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        System.out.println("迁移参数保存接口JSON.toJSONString(configInfo)==="+JSON.toJSONString(configInfo));
        return migrationService.saveConfig(pageController, configInfo);
    }

    /**
     * @description: 获取字段类型映射
     * @author zzg
     * @date 2021/1/12 13:51
     */
    @GetMapping(value = "/getTypeMapping")
    public Result getTypeMapping(HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        return migrationService.getTypeMapping(pageController);
    }

    /**
     * @description: 保存字段类型映射
     * @author zzg
     * @date 2021/1/12 13:52
     */
    @PostMapping(value = "/saveTypeMapping")
    public Result saveTypeMapping(@RequestBody List<SourceTypesInfo> sourceTypesInfos, HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        System.out.println("保存字段类型映射JSON.toJSONString(sourceTypesInfos)==="+JSON.toJSONString(sourceTypesInfos));

        return migrationService.saveTypeMapping(sourceTypesInfos, pageController);
    }

    /**
     * @description: 获取所有模式信息
     * @author zzg
     * @date 2021/1/12 13:52
     */
    @PostMapping(value = "/getAllPattern")
    public Result getAllPattern(HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        return migrationService.getAllPattern(pageController);
    }

    /**
     * @description: 获取表空间信息
     * @author zzg
     * @date 2021/1/12 13:51
     */
    @PostMapping(value = "/getTableSpace")
    public Result getTableSpace(HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        return migrationService.getTableSpace(pageController);
    }


    /**
     * @description: 根据模式id获取模式下所有信息
     * @author zzg
     * @date 2021/1/12 13:52
     */
    @PostMapping(value = "/getPatternDataById")
    public Result getPatternDataById(@RequestParam String id, HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        Result patternDataById = migrationService.getPatternDataById(pageController, id);
        return patternDataById;
    }


    /**
     * @description: 通过类型名称获取模式改模式下的类型信息
     * @author zzg
     * @date 2021/1/12 13:47
     */
    @PostMapping(value = "/getPatternDataByTypeName")
    public Result getPatternDataByTypeName(@RequestParam String patternName, @RequestParam String typeName, HttpServletRequest request) {
        return Result.ok();
    }

    /**
     * @description: 获取表下的所有信息
     * @author zzg
     * @date 2021/1/12 13:53
     */
    @PostMapping(value = "/getAllTableData")
    public Result getAllTableData(@RequestParam String patternName, @RequestParam String tableName, HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        return migrationService.getAllTableData(pageController, patternName, tableName);
    }

    /**
     * @description: 批量获取表下的所有信息
     * @author zzg
     * @date 2021/1/12 13:53
     */
    @PostMapping(value = "/batchGetAllTableData")
    public Result batchGetAllTableData(@RequestBody List<TableInfo> tables, HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        return migrationService.batchGetAllTableData(pageController, tables);
    }

    /**
     * @description: 获取表下的信息通过类型名称
     * @author zzg
     * @date 2021/1/12 13:54
     */
    @PostMapping(value = "/getTableDataByTypeName")
    public Result getTableDataByTypeName(@RequestParam String patternName, @RequestParam String tableName,
                                         @RequestParam String typeName, HttpServletRequest request) {
        return Result.ok();
    }

    /**
     * @description: 保存迁移参数
     * @author zzg
     * @date 2021/1/12 13:55
     */
    @PostMapping(value = "/saveMigrationInfo")
    public Result saveMigrationInfo(@RequestBody MigrationConfigInfo migrationConfigInfo, HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        return migrationService.saveMigrationInfo(pageController, migrationConfigInfo);
    }

    /**
     * @description: 保存模式信息
     * @author zzg
     * @date 2021/1/12 13:55
     */
    @PostMapping(value = "/saveSchemaInfo")
    public Result saveSchemaInfo(@RequestBody List<SchemaInfo> schemaInfos, HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        return migrationService.saveSchemaInfo(pageController, schemaInfos);
    }

    /**
     * @description: 开始迁移
     * @author zzg
     * @date 2021/1/12 13:56
     */
    @PostMapping(value = "/startMigration")
    public Result startMigration(HttpServletRequest request) {
        PageController pageController = UserResourceUtils.getPageController(request);
        if (pageController == null) {
            return Result.error("连接超时,请重新登录");
        }
        return migrationService.startMigration(pageController);
    }
}
