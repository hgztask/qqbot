package com.example.qqbot.data;

/**
 * QQ通信相关地址
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 13:29
 */
public interface MailingAddress {


    /**
     * 发送消息通信地址
     */
    String SEND_MESSAGE = "http://127.0.0.1:5700";

    /**
     * 监听消息通信地址
     */
    String LISTENING_ADDRESS = "ws://127.0.0.1:9090";

}