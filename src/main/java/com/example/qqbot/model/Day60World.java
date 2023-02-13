package com.example.qqbot.model;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.group.DataGroup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 每天60秒看世界逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 23:28
 */
@Slf4j
public class Day60World implements Runnable {

    private static final File PathFile = new File("E:\\每日60秒世界记录.json");
    /**
     * 记录每个群的状态
     */
    private static Map<String, Integer> dataTime = Day60World.getFileDateDay(PathFile);

    @Getter
    private static Day60World day60World = new Day60World();

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
    private  static Map<String, Integer> getFileDateDay(File file) {
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
    private  static boolean sendDay60WorldContent(@NonNull String group_id) {
        if (SignalUtil.sendGroupMessage(group_id, "[CQ:image,file=每日60秒看世界,url=http://www.ggapi.cn/api/60s]").isEmpty()) {
            return false;
        }
        return true;
    }


    @Override
    public  void run() {
        if (dataGroup == null) {
            throw new NullPointerException("dataGroup 为null,请通过start方法设置");
        }
        //获取当天的日
        int dateDAY_of_month = InformationUtil.getDateDAY_OF_MONTH();
        //群号
        String group_id = dataGroup.getGroup_id();
        System.out.println(group_id);

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


}










