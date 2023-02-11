package com.example.qqbot.model;

import cn.hutool.json.JSONObject;
import com.example.qqbot.SignalUtil;
import com.example.qqbot.data.DataPrivate;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * 私聊模块逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 15:29
 */
public class PrivateModel implements Runnable {
    private DataPrivate dataPrivate;

    public PrivateModel(DataPrivate dataPrivate) {
        this.dataPrivate = dataPrivate;
    }

    @Override
    public void run() {
        //实现一个复读机
        HashMap<String, String> data = new HashMap<>();
        data.put("user_id", dataPrivate.getUser_id());
        data.put("message", dataPrivate.getMessage());
        JSONObject json = SignalUtil.httpGet(DataPrivate.getENDPOINT(), data);
        if (json.isEmpty()) {
            System.out.println("发送消息失败!");
            return;
        }
        System.out.println("发送成功!:" + json);


    }


}
