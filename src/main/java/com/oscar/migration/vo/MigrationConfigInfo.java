package com.oscar.migration.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zzg
 * @description: 迁移配置信息
 * @date 2021/1/19 13:38
 */
@Data
public class MigrationConfigInfo {
    /**是否开启了强制迁移*/
    private Boolean isOpenConstraintMigrate;
    /**选中的强制迁移模式名*/
    private String checkTargetSchemaName;
    /**选择的迁移范围*/
    private int checkMigrationCircle;
}
