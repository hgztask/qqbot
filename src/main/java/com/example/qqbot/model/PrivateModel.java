package com.example.qqbot.model;

import cn.hutool.json.JSONObject;
import com.example.qqbot.SignalUtil;
import com.example.qqbot.data.DataPrivate;
import com.example.qqbot.data.DataUserEights;
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
        //数组形式
        String message = dataPrivate.getMessage();
        //原始消息的内容
        String raw_message = dataPrivate.getRaw_message();
        String user_id = dataPrivate.getUser_id();
        if (raw_message.startsWith("刷新复读机成员") && DataUserEights.SUPERUSER.contains(user_id)) {
            //需要超级用户权限
            //该关键词触发条件要优先于下面的复读机,要不然会导致复读操作
            ReReadingModel.readFileArraySetRE_READING_MEMBER_SET(user_id);
            return;
        }
        //实现一个复读机
        HashMap<String, String> data = new HashMap<>();
        data.put("user_id", dataPrivate.getUser_id());
        data.put("message", message);
        JSONObject json = SignalUtil.httpGet(SignalUtil.getPRIVATEENDPOINT(), data);
        if (json.isEmpty()) {
            System.out.println("发送消息失败!");
            return;
        }
        System.out.println("发送成功!:" + json);


    }


}
