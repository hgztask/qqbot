package com.example.qqbot.model;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.SignalUtil;
import com.example.qqbot.data.DataGroup;
import com.example.qqbot.data.DataUserEights;
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
    private static final File PATH_FILE = new File("E:\\复读成员.json");
    /**
     * 指定要复读机的成员
     */
    @Getter
    private static List<String> RE_READING_MEMBER_SET = getFileJson(PATH_FILE);

    public ReReadingModel(DataGroup dataGroup) {
        this.dataGroup = dataGroup;
    }

    /**
     * 读取本地的复读机成员
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
        if (RE_READING_MEMBER_SET.contains(userATID)) {
            JSONObject json = SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "该用户已经设置过了!");
            if (json.isEmpty()) {
                log.info("设置要复读的人员功能模块-回复设置过-请求失败了");
            }
            return;
        }
        RE_READING_MEMBER_SET.add(userATID);
        //修改本地文件
        FileUtil.writeUtf8String(JSONUtil.parseArray(RE_READING_MEMBER_SET).toStringPretty(), PATH_FILE);
        JSONObject json = SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "已将该成员设置为复读机对象");
        if (json.isEmpty()) {
            log.info("设置要复读的人员功能模块-已将该成员设置对象-请求失败了");
            return;
        }
        log.info("设置要复读的人员功能模块-已将该成员设置对象-请求成功!");
    }


    /**
     * 读取本地复读机成员对象并重新赋值给RE_READING_MEMBER_SET变量,相当于刷新了该变量的值
     * 触发地点群聊
     *
     * @param dataGroup 群聊数据层
     */
    @SuppressWarnings("all")
    public static void readFileArraySetRE_READING_MEMBER_SET(DataGroup dataGroup) {
        RE_READING_MEMBER_SET = getFileJson(PATH_FILE);
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "已读取本地复读机成员对象并刷新值");
    }


    /**
     * 读取本地复读机成员对象并重新赋值给RE_READING_MEMBER_SET变量,相当于刷新了该变量的值
     * 触发地点私聊
     */
    @SuppressWarnings("all")
    public static void readFileArraySetRE_READING_MEMBER_SET(String user_id) {
        RE_READING_MEMBER_SET = getFileJson(PATH_FILE);
        SignalUtil.sendPrivateMessage(user_id, "已读取本地复读机成员对象并刷新值");
    }


    /**
     * 指定删除的复读机成员对象
     *
     * @param dataGroup
     */
    public static void removeReReadingMemberSet(DataGroup dataGroup, String userATID) {
        if (RE_READING_MEMBER_SET.remove(userATID)) {
            //修改本地文件
            FileUtil.writeUtf8String(JSONUtil.parseArray(RE_READING_MEMBER_SET).toStringPretty(), PATH_FILE);
            if (SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "已将该对象移除至集合对象!").isEmpty()) {
                log.info("指定删除的复读机成员对象-已将该对象移除至集合对象-请求成功!");
                return;
            }
            log.info("指定删除的复读机成员对象-请求成功!");
            return;
        }
        if (SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "移除失败,没有该复读机成员对象!").isEmpty()) {
            log.info("指定删除的复读机成员对象-移除失败-请求成功!");
            return;
        }
        log.info("指定删除的复读机成员对象-移除失败-请求成功!");
    }

    /**
     * 打印复读机集合里的对象
     */
    public static void printReReadingMemberSet(DataGroup dataGroup) {
        if (SignalUtil.sendPrivateMessage(dataGroup.getUser_id(), String.format("复读机对象成员:\n%s", JSONUtil.parseArray(RE_READING_MEMBER_SET).toStringPretty())).isEmpty()) {
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "打印复读机集合里的对象-响应失败!");
            return;
        }
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "信息已私发到超级用户上!");
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
