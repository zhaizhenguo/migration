package com.oscar.migration.server;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @description: netty服务入口
 * @author zzg
 * @date 2021/2/1 13:35
 */
@Component
@Slf4j
public class SocketIoRunner implements CommandLineRunner {
    private final SocketIOServer server;

    @Autowired
    public SocketIoRunner(SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void run(String... args) {
        log.info("---------- NettySocketIO通知服务开始启动 ----------");
        server.start();
        log.info("---------- NettySocketIO通知服务启动成功 ----------");
    }

}