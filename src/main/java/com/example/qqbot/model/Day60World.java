package com.example.qqbot.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.CQ.CQCode;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 每天60秒看世界逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 23:28
 */
@Slf4j
@Component
public class Day60World implements Runnable, IMessageEvent {

    private static final File PathFile = new File("E:\\每日60秒世界记录.json");
    /**
     * 记录每个群的状态
     */
    private static Map<String, Integer> dataTime = Day60World.getFileDateDay(PathFile);


    @Setter
    private DataGroup dataGroup;

    private Day60World() {
    }


    /***
     *从本地文件读取群记录信息
     * @param file 文件路径
     * @return map对象
     */
    @SuppressWarnings("all")
    private static Map<String, Integer> getFileDateDay(File file) {
        Map jsonObj;
        try {
            jsonObj = JSONUtil.readJSONObject(file, StandardCharsets.UTF_8);
        } catch (IORuntimeException e) {
            //如果获取不到文件
            return new HashMap<>();
        }
        if (jsonObj.isEmpty()) {
            return new HashMap<>();
        }
        return new HashMap<>(jsonObj);
    }


    /**
     * 发送60秒看世界内容到群里
     *
     * @return 是否成功发送
     */
    private static boolean sendDay60WorldContent(@NonNull String group_id) {
        Connection.Response response = SignalUtil.jsoupHttpGet("https://60s.viki.moe/", false);
        if (response == null) {
            return false;
        }
        String body = response.body();
        if (!(JSONUtil.isTypeJSONObject(body))) {
            return false;
        }
        List<String> jsonArray = JSONUtil.parseObj(body).getByPath("data", List.class);
        StringBuilder stringBuilder = new StringBuilder("60秒看世界!\n");
        for (String s : jsonArray) {
            stringBuilder.append(s);
            stringBuilder.append("\n");
        }
        if (jsonArray.isEmpty()) {
            return false;
        }
        if (SignalUtil.sendGroupMessage(group_id, stringBuilder.toString().trim()).isEmpty()) {
            return false;
        }
        return true;
    }


    @Override
    public void run() {
        //获取当天的日
        int dateDAY_of_month = InformationUtil.getDateDAY_OF_MONTH();
        //群号
        String group_id = dataGroup.getGroup_id();

        Set<String> keySet = dataTime.keySet();
        //如果集合里有登记的群聊,则判断集合内的更新日期
        //结果都是添加并写入本地文件,可以利用set集合的特点
        if (keySet.contains(group_id)) {
            for (String g : keySet) {
                //当天数相同时不更新处理
                if (dataTime.get(g) == dateDAY_of_month) {
                    continue;
                }
                if (sendDay60WorldContent(group_id)) {
                    //更新对应的value至最新
                    dataTime.put(g, dateDAY_of_month);
                    FileUtil.writeUtf8String(JSONUtil.parseObj(dataTime).toStringPretty(), PathFile);
                    log.info(group_id + "群60秒看世界消息推送成功!");
                    return;
                }
                log.info(group_id + "群60秒看世界消息推送失败!");
            }
            return;
        }
        if (sendDay60WorldContent(group_id)) {
            //如果集合里没有有登记的群聊,则添加群聊并发送消息
            dataTime.put(group_id, dateDAY_of_month);
            FileUtil.writeUtf8String(JSONUtil.parseObj(dataTime).toStringPretty(), PathFile);
            log.info(group_id + "群60秒看世界消息推送成功!");
            return;
        }
        log.info(group_id + "群60秒看世界消息推送失败!");

    }


    /**
     * 权重,权重高的值会先匹配
     * 该功能权重要求比群聊高1个
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
    @SuppressWarnings("all")
    public boolean onMessage(JSONObject jsonObject, Message message) {
        if (!("group".equals(message.getMessage_type()))) {
            return false;
        }
        //this.dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
        //this.run();
        return false;
    }
}










