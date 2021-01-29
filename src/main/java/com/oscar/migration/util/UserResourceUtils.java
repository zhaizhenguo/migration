package com.oscar.migration.util;

import com.oscar.migration.constants.SysConstants;
import com.oscar.migration.entity.UserResource;
import com.oscar.ui.common.controller.PageController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author zzg
 * @description: 迁移参数工具类
 * @date 2021/1/14 13:42
 */
@Slf4j
public class UserResourceUtils {

    /**
     * @description: 获取当前用户的UserResource
     * @author zzg
     * @date 2021/1/15 16:48
     */
    public static UserResource getUserResource(HttpServletRequest request) {
        UserResource userResource = null;
        HttpSession session = request.getSession(false);
        String sessionId = session.getId();
        if (SysConstants.UserResourceMap.containsKey(sessionId)) {
            userResource = SysConstants.UserResourceMap.get(sessionId);
        }
        return userResource;
    }

    /**
     * @description: 获取当前用户的pageController
     * @author zzg
     * @date 2021/1/15 16:48
     */
    public static synchronized PageController getPageController(HttpServletRequest request) {
        PageController controller = null;
        HttpSession session = request.getSession(false);
        String sessionId = session.getId();
        log.info("获取当前用户的pageController sessionId{}", sessionId);
        if (SysConstants.UserResourceMap.containsKey(sessionId)) {
            UserResource userResource = SysConstants.UserResourceMap.get(sessionId);
            controller = userResource.getMigController();
            if (controller == null) {
                log.info("获取当前用户的pageController 创建了新工程{}", controller);
                controller = new PageController();
                userResource.setMigController(controller);
            }
        }
        return controller;
    }

    /**
     * @description: 查询此用户是否创建了工程
     * @author zzg
     * @date 2021/1/14 14:40
     */
    public static synchronized boolean isCreateProject(HttpServletRequest request) {
        String sessionId = getSessionId(request);
        log.info("查询此用户是否创建了工程 sessionId{}", sessionId);
        if (StringUtils.isBlank(sessionId)) {
            log.error("session为空！");
            return false;
        }
        if (SysConstants.UserResourceMap.containsKey(sessionId)) {
            UserResource userResource = SysConstants.UserResourceMap.get(sessionId);
            return userResource.isCreateProject();
        }
        return false;
    }

    /**
     * @description: 更新IsCreateProject状态
     * @author zzg
     * @date 2021/1/15 16:47
     */
    public static synchronized void setIsCreateProject(HttpServletRequest request, boolean boo) {
        String sessionId = getSessionId(request);
        log.info("更新IsCreateProject状态 sessionId{}", sessionId);

        if (StringUtils.isBlank(sessionId)) {
            log.error("session为空！");
        }
        if (SysConstants.UserResourceMap.containsKey(sessionId)) {
            UserResource userResource = SysConstants.UserResourceMap.get(sessionId);
            userResource.setCreateProject(boo);
        }
    }

    private static synchronized String getSessionId(HttpServletRequest request) {
        String sessionId = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            sessionId = session.getId();
        }
        return sessionId;
    }
}
