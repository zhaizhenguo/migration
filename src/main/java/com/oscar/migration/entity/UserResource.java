package com.oscar.migration.entity;

import com.oscar.ui.common.controller.PageController;
import lombok.Data;

import java.util.Map;

/**
 * @author zzg
 * @description: 用户资源类
 * @date 2021/1/13 10:28
 */
@Data
public class UserResource {
    /**用户sessionID*/
    private String sessionId;
    /**用户秘钥对*/
    private Map<String, String> keyPairMap;
    /**迁移执行对象*/
    private PageController migController;
    /**是否创建了工程*/
    private boolean isCreateProject;
}
