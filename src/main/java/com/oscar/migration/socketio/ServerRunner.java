package com.oscar.migration.socketio;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @description: netty服务入口
 * @author zzg
 * @date 2021/2/1 13:35
 */
@Component
@Order(value=1)
@Slf4j
public class ServerRunner implements CommandLineRunner {
    private final SocketIOServer server;

    @Autowired
    public ServerRunner(SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("socket.io启动成功！");
        server.start();
    }

}



