package com.oscar.migration.config;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzg
 * @description: socketIo配置
 * @date 2021/2/1 13:28
 */
@Configuration
@Slf4j
public class NettySocketConfig {

    @Value("${socketIo.port}")
    private Integer port;

    @Value("${socketIo.workCount}")
    private int workCount;

    @Value("${socketIo.allowCustomRequests}")
    private boolean allowCustomRequests;

    @Value("${socketIo.upgradeTimeout}")
    private int upgradeTimeout;

    @Value("${socketIo.pingTimeout}")
    private int pingTimeout;

    @Value("${socketIo.pingInterval}")
    private int pingInterval;

    @Value("${socketIo.maxFramePayloadLength}")
    private int maxFramePayloadLength;

    @Value("${socketIo.maxHttpContentLength}")
    private int maxHttpContentLength;

    /**
     * 创建Socket，并设置监听端口
     */
    @Bean
    public SocketIOServer socketIOServer() {

        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        // SocketIO端口
        config.setPort(port);
        // 连接数大小
        config.setWorkerThreads(workCount);
        config.setAllowCustomRequests(allowCustomRequests);
        // 协议升级超时时间（毫秒），默认10000。HTTP握手升级为ws协议超时时间
        config.setUpgradeTimeout(upgradeTimeout);
        // Ping消息间隔（毫秒），默认25000。客户端向服务器发送一条心跳消息间隔
        config.setPingInterval(pingInterval);
        // Ping消息超时时间（毫秒），默认60000，这个时间间隔内没有接收到心跳消息就会发送超时事件
        config.setPingTimeout(pingTimeout);
        // 设置HTTP交互最大内容长度
        config.setMaxHttpContentLength(maxHttpContentLength);
        // 设置最大每帧处理数据的长度，防止他人利用大数据来攻击服务器
        config.setMaxFramePayloadLength(maxFramePayloadLength);
        config.setAuthorizationListener(data -> {
            //返回为true直接通过，不做登录控制;
            return true;
        });

        return new SocketIOServer(config);
    }

    /**
     * 开启SocketIOServer注解支持
     */
    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        return new SpringAnnotationScanner(socketServer);
    }

}

