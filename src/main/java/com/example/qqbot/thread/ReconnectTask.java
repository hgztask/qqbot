package com.example.qqbot.thread;

import com.example.qqbot.Client;
import com.example.qqbot.data.MailingAddress;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/27 14:13
 */
public class ReconnectTask implements Runnable {
    @Override
    public void run() {
        while (true) {
            if (Client.connect(MailingAddress.LISTENING_ADDRESS)) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final ReconnectTask reconnectTask = new ReconnectTask();

    public static void execute() {
        @SuppressWarnings("all")
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            executorService.execute(reconnectTask);
        } finally {
            executorService.shutdown();
        }

    }

}
