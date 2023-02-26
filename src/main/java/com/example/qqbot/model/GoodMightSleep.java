package com.example.qqbot.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.json.DataJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 早中午晚安的逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 18:11
 */
@Slf4j
@Component
public class GoodMightSleep implements Runnable, IMessageEvent {

    private DataGroup dataGroup;


    private static final List<String> LIST = ListUtil.toList("晚安","睡觉","晚上好", "早上好", "中午好", "下午好");


    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 6;
    }

    /**
     * 接受消息
     *
     * @param jsonObject 原始消息对象
     * @param message    消息对象
     * @return 是否匹配成功
     */
    @Override
    public boolean onMessage(JSONObject jsonObject, Message message) {
        if (!("group".equals(message.getMessage_type()))) {
            return false;
        }
        this.dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
        String raw_message = dataGroup.getRaw_message();
        if (!(InformationUtil.isContainsMessAge(LIST,raw_message))) {
            return false;
        }
        String user_id = dataGroup.getUser_id();
        String group_id = dataGroup.getGroup_id();
        int hh = Integer.parseInt(DateUtil.format(DateUtil.date(), "HH"));
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(DataJson.at(user_id));
        if ((hh >= 19) && (hh <= 5)) {
            SignalUtil.sendGroupMessage(group_id,"晚上好!");
            return true;
        }
        if ((hh >= 6) && (hh <= 10)) {
            SignalUtil.sendGroupMessage(group_id,"早上好!");
            return true;
        }
        if ((hh >= 11) && (hh <= 13)) {
            SignalUtil.sendGroupMessage(group_id,"中午好!");
            return true;
        }
        if ((hh >= 14) && (hh <= 18)) {
            SignalUtil.sendGroupMessage(group_id,"下午好!");
            return true;
        }
        return false;
    }


    @Override
    public void run() {

    }
}
