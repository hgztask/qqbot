package com.example.qqbot.model;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.SignalUtil;
import com.example.qqbot.data.DataGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 复读逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 21:31
 */
@Slf4j
public class ReReadingModel implements Runnable {

    /**
     * 群聊数据层
     */
    private DataGroup dataGroup;


    /**
     * 指定存储复读机成员的路径
     */
    private static final File MEMBER_PATH_FILE = new File("E:\\复读成员.json");

    /**
     * 存储自动触发复读机的关键词的路径
     */
    private static final File KEYPATH_FILE = new File("E:\\复读关键词.json");
    /**
     * 复读机的成员集合
     */
    @Getter
    private static List<String> MEMBER_SET = getFileJson(MEMBER_PATH_FILE);

    /**
     * 自动触发复读机的关键词集合
     */
    private static List<String> KEY_SET = getFileJson(KEYPATH_FILE);

    public ReReadingModel(DataGroup dataGroup) {
        this.dataGroup = dataGroup;
    }

    /**
     * 读取本地的复读集合对象
     *
     * @return arrayList对象
     */
    private static ArrayList<String> getFileJson(File file) {
        if (!(file.isFile())) {
            return new ArrayList<>(0);
        }
        List jsonArray = JSONUtil.readJSONArray(file, StandardCharsets.UTF_8);
        System.out.println(jsonArray.toString());
        return new ArrayList<>(jsonArray);
    }


    /**
     * 添加要复读的人员
     */
    public static void addReReadingMemberSet(DataGroup dataGroup, String userATID) {
        if (MEMBER_SET.contains(userATID)) {
            JSONObject json = SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "该用户已经设置过了!");
            if (json.isEmpty()) {
                log.info("设置要复读的人员功能模块-回复设置过-请求失败了");
            }
            return;
        }
        MEMBER_SET.add(userATID);
        //修改本地文件
        FileUtil.writeUtf8String(JSONUtil.parseArray(MEMBER_SET).toStringPretty(), MEMBER_PATH_FILE);
        JSONObject json = SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), String.format("已将该成员%s设置为复读机对象", userATID));
        if (json.isEmpty()) {
            log.info("设置要复读的人员功能模块-已将该成员设置对象-请求失败了");
            return;
        }
        log.info("设置要复读的人员功能模块-已将该成员设置对象-请求成功!");
    }


    /**
     * 添加触发复读机关键词
     *
     * @param key 关键词
     */
    public static void addKeySet(DataGroup dataGroup, String key) {
        if (KEY_SET.contains(key)) {
            JSONObject json = SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "该关键词已经设置过了!");
            if (json.isEmpty()) {
                log.info("添加触发复读机关键词-回复设置过-请求失败了");
            }
            return;
        }
        KEY_SET.add(key);
        //修改本地文件
        FileUtil.writeUtf8String(JSONUtil.parseArray(KEY_SET).toStringPretty(), KEYPATH_FILE);
        JSONObject json = SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "已添加该关键词作为复读机触发条件之一=" + key);
        if (json.isEmpty()) {
            log.info("添加触发复读机关键词-发送消息-请求失败了");
            return;
        }
        log.info("添加触发复读机关键词-发送消息-请求成功!");
    }


    /**
     * 移除指定触发复读机关键词
     */
    public static void removeKeySet(DataGroup dataGroup, String key) {
        if (KEY_SET.remove(key)) {
            //修改本地文件
            FileUtil.writeUtf8String(JSONUtil.parseArray(KEY_SET).toStringPretty(), KEYPATH_FILE);
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "已将该关键词移除集合对象!");
            return;
        }
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "移除失败,没有该复读机关键词!");
    }


    /**
     * 打印触发复读机关键词集合
     *
     * @param user_id
     */
    public static void printKeySet(String user_id) {
        String toStringPretty = JSONUtil.parseArray(KEY_SET).toStringPretty();
        if (SignalUtil.sendPrivateMessage(user_id, toStringPretty).isEmpty()) {
            log.info("打印触发复读机关键词集合-失败!");
            return;
        }
        log.info("打印触发复读机关键词集合-失败!");
    }


    /**
     * 读取本地复读机成员对象并重新赋值给RE_READING_MEMBER_SET变量,相当于刷新了该变量的值
     * 触发地点私聊
     */
    @SuppressWarnings("all")
    public static void readFileArraySetRE_READING_MEMBER_SET(String user_id) {
        MEMBER_SET = getFileJson(MEMBER_PATH_FILE);
        SignalUtil.sendPrivateMessage(user_id, "已读取本地复读机成员对象并刷新值");
    }


    /**
     * 指定删除的复读机成员对象
     *
     * @param dataGroup
     */
    public static void removeReReadingMemberSet(DataGroup dataGroup, String userATID) {
        if (MEMBER_SET.remove(userATID)) {
            //修改本地文件
            FileUtil.writeUtf8String(JSONUtil.parseArray(MEMBER_SET).toStringPretty(), MEMBER_PATH_FILE);
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "已将该对象移除至集合对象!");
            return;
        }
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "移除失败,没有该复读机成员对象!");
    }

    /**
     * 打印触发复读机关键词集合里的对象
     * <p>
     * 发送到私聊窗口
     */
    public static void printReReadingMemberSet(String user_id) {
        if (SignalUtil.sendPrivateMessage(user_id, String.format("复读机对象成员:\n%s", JSONUtil.parseArray(MEMBER_SET).toStringPretty())).isEmpty()) {
            log.info("打印复读机集合里的对象模块功能消息发送失败!");
            return;
        }
        log.info("打印复读机集合里的对象模块功能消息发送成功!");
    }


    @Override
    public void run() {
        String raw_message = dataGroup.getRaw_message();
        JSONObject json = SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), raw_message);
        if (json.isEmpty()) {
            return;
        }
        log.info("复读机请求成功!");
    }


}
