package com.oscar.migration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

/**
 * @description: WebSocket配置
 * @author zzg
 * @date 2021/1/13 15:04
 */
@Configuration
public class WebSocketConfig extends Configurator{


    /** 修改握手,就是在握手协议建立之前修改其中携带的内容 */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        System.out.println("调用modifyHandshake方法...");
        HttpSession session = (HttpSession) request.getHttpSession();
        if (session!=null){
            System.out.println("获取到session id:"+session.getId());
            sec.getUserProperties().put(HttpSession.class.getName(),session);
        }else{
            System.out.println("modifyHandshake 获取到null session");
        }
    }


    @Bean
    public ServerEndpointExporter serverEndpointExporter () {
        return new ServerEndpointExporter();
    }
}
