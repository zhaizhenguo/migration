package com.oscar.migration.controller;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.oscar.migration.constants.SysConstants;
import com.oscar.migration.util.UserResourceUtils;
import com.oscar.migration.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zzg
 * @description: socketIo控制层
 * @date 2021/2/1 13:36
 */
@RestController
@Slf4j
public class SocketIOController {


    @Resource
    SocketIOServer socketIoServer;

    /**
     * @description: 当客户端发起连接时调用
     * @author zzg
     * @date: 2021/2/1 16:44
     * @param: [client]
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        if (client != null) {
            String sessionId = UserResourceUtils.getSessionId(client);
            log.info("当前sessionID==={}", sessionId);
            //判断当前用户是否已登录并创建资源
            if (StringUtils.isBlank(sessionId) || !SysConstants.UserResourceMap.containsKey(sessionId)) {
                log.error("未找到用户资源");
                client.sendEvent("msgEvent", Result.error("未找到用户资源,请重新登录"));
                return;
            }
            //设置socket客户端
            SysConstants.UserResourceMap.get(sessionId).setClient(client);
            log.info("建立连接成功");
            client.sendEvent("msgEvent", Result.ok());
        } else {
            log.error("客户端为空");
        }
    }


    /**
     * @description: 客户端断开连接时调用
     * @author zzg
     * @date: 2021/2/1 13:53
     * @param: [client]
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String sessionId = UserResourceUtils.getSessionId(client);
        log.info("客户端断开连接,当前sessionID==={}", sessionId);
        //判断当前用户是否已登录并创建资源
        if (StringUtils.isNotBlank(sessionId) && SysConstants.UserResourceMap.containsKey(sessionId)) {
            log.info("客户端断开连接,已清空当前用户资源");
            SysConstants.UserResourceMap.remove(sessionId);
        }
        client.disconnect();
    }

    /**
     * @description: socket事件消息接收入口
     * @author zzg
     * @date: 2021/2/1 17:02
     * @param: [client, ackRequest, msg]
     */
    @OnEvent("msgEvent")
    public void onEvent(SocketIOClient client, AckRequest ackRequest, String msg) {
        log.info("接收到客户端消息msg==={}", msg);
        log.info("client==={}", client);
        log.info("ackRequest==={}", ackRequest);
    }

}
