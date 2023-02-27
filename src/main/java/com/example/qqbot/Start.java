package com.example.qqbot;

import com.example.qqbot.data.MailingAddress;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/27 13:56
 */
@Component
public class Start implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        if (!Client.connect(MailingAddress.LISTENING_ADDRESS)) {
            //如果第一次启动时连接失败,也调用重连方法
            Client.reConnect();
        }
    }
}
