package com.oscar.migration.controller;

import com.oscar.migration.webSocket.WebSocketServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/ws")
public class WebSocketController {



    /**
     * @description: 群发消息内容
     * @author zzg
     * @date: 2021/1/29 22:12
     * @param: [message]
     * @return: java.lang.String
     */
    @RequestMapping(value="/sendAll", method= RequestMethod.GET)
    public String sendAllMessage(@RequestParam() String message){
        try {
            WebSocketServer.BroadCastInfo(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    /**
     * 指定会话ID发消息
     * @param message 消息内容
     * @param id 连接会话ID
     * @return
     */
    @RequestMapping(value="/sendOne", method= RequestMethod.GET)
    public String sendOneMessage(@RequestParam(required=true) String message, @RequestParam(required=true) String id){
        try {
            WebSocketServer.SendMessage(message,id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ok";
    }
}
