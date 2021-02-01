package com.oscar.migration.config;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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


    public static final String JSESSIONID = "JSESSIONID";

    @Bean
    public SocketIOServer socketIOServer() {
        /*
         * 创建Socket，并设置监听端口
         */
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        // 设置主机名，默认是0.0.0.0
        // config.setHostname("localhost");
        // 设置监听端口
        config.setPort(8082);
        // 协议升级超时时间（毫秒），默认10000。HTTP握手升级为ws协议超时时间
        config.setUpgradeTimeout(10000);
        // Ping消息间隔（毫秒），默认25000。客户端向服务器发送一条心跳消息间隔
        config.setPingInterval(60000);
        // Ping消息超时时间（毫秒），默认60000，这个时间间隔内没有接收到心跳消息就会发送超时事件
        config.setPingTimeout(180000);
        // 这个版本0.9.0不能处理好namespace和query参数的问题。所以为了做认证必须使用全局默认命名空间
        config.setAuthorizationListener(new AuthorizationListener() {
            @Override
            public boolean isAuthorized(HandshakeData data) {
                String cookie = data.getHttpHeaders().get(HttpHeaderNames.COOKIE);
                //未登录用户
                if (StringUtils.isBlank(cookie) || !cookie.contains(JSESSIONID)){
                    log.error("建立连接失败，sessionID不存在");
                    return false;
                }
                //返回为true直接通过，不做登录控制;
                return true;
            }
        });

        return new SocketIOServer(config);
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        return new SpringAnnotationScanner(socketServer);
    }

}

