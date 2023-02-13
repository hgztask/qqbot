package com.example.qqbot.model.group;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.group.DataGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 监听群消息
 * <p>
 * 功能
 * 监听指定群聊的消息,当有更新时.将对应的消息原封不动通过机器人推送到指定的推送全
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/12 22:25
 */
@Slf4j
public class ListeningGroupModel implements Runnable {

    /**
     * 单例对象
     */
    @Getter
    private static final ListeningGroupModel listeningGroupModel = new ListeningGroupModel();


    /**
     * 监听的群
     */
    private static final Set<String> LISTENINGGROUPIDSET = new LinkedHashSet<>();


    /**
     * 推送消息的群聊
     */
    private static final Set<String> PUSHGROUPIDSET = new LinkedHashSet<>();


    static {
        LISTENINGGROUPIDSET.add("935671622");
        LISTENINGGROUPIDSET.add("942611877");
        PUSHGROUPIDSET.add("760849278");
    }

    @Setter
    private DataGroup dataGroup;

    @Override
    public void run() {
        if (dataGroup == null) {
            throw new NullPointerException("dataGroup为null");
        }
        if (!(LISTENINGGROUPIDSET.contains(dataGroup.getGroup_id()))) {
            return;
        }
        if ("202619505".equals(dataGroup.getUser_id())) {
            //如果监听的群聊里触发该功能的包含了该成员.则不推送
            return;
        }

        for (String pushGroupID : PUSHGROUPIDSET) {
            if (SignalUtil.sendGroupMessage(pushGroupID, dataGroup.getRaw_message()).isEmpty()) {
                log.info("监听群聊推送消息失败!");
                return;
            }
            log.info("监听群聊推送消息成功!");

        }

    }


    /**
     * 打印接受推送消息的群聊集合
     * 推荐私聊窗口
     *
     * @param user_id 用户
     */
    public static void printPushGather(String user_id) {
        if (SignalUtil.sendPrivateMessage(user_id, "接受推送消息的群聊集合:" + JSONUtil.parseArray(PUSHGROUPIDSET).toStringPretty()).isEmpty()) {
            log.info("打印接受推送消息的群聊集合-失败!");
            return;
        }
        log.info("打印接受推送消息的群聊集合-成功!");
    }

    /**
     * 打印指定内容消息
     * 判断指定群聊是否是标记了推送状态
     *
     * @param group_id 群号
     */
    public static void printIsPush(String group_id) {
        if (PUSHGROUPIDSET.contains(group_id)) {
            SignalUtil.sendGroupMessage(group_id, "当前群聊推送状态=true");
            return;
        }
        SignalUtil.sendGroupMessage(group_id, "当前群聊标记推送状态=false");
    }

    /**
     * 打印指定内容消息
     * 判断指定群聊是否是标记了推送状态并推送给指定用户
     *
     * @param group_id 判断的群号
     * @param user_id  推送的用户
     */
    public static void printIsPush(String group_id, String user_id) {
        if (PUSHGROUPIDSET.contains(group_id)) {
            SignalUtil.sendPrivateMessage(user_id, "指定的群聊推送状态=true");
            return;
        }
        SignalUtil.sendPrivateMessage(user_id, "指定的群聊推送状态=false");
    }

    /**
     * 打印指定内容消息
     * 判断指定群聊是否标记了监听状态并发送消息给指定群聊
     *
     * @param group_id 要判断的群号
     */
    public static void printIslistening(String group_id) {
        if (LISTENINGGROUPIDSET.contains(group_id)) {
            SignalUtil.sendGroupMessage(group_id, "当前群聊监听状态=true");
            return;
        }
        SignalUtil.sendGroupMessage(group_id, "当前群聊监听状态=false");
    }

    /**
     * 打印指定内容消息
     * 判断指定群聊是否标记了监听状态并发送消息给指定QQ号对象
     *
     * @param group_id 要判断的群号
     * @param user_id  发送给消息的QQ号
     */
    public static void printIslistening(String group_id, String user_id) {
        if (LISTENINGGROUPIDSET.contains(group_id)) {
            SignalUtil.sendPrivateMessage(user_id, "当前群聊监听状态=true");
            return;
        }
        SignalUtil.sendPrivateMessage(user_id, "当前群聊监听状态=false");
    }


    /**
     * 打印监听群聊的集合
     *
     * @param user_id
     */
    public static void printlisteninggroupGather(String user_id) {
        JSONObject json = SignalUtil.sendPrivateMessage(user_id, "监听群聊的集合的对象:\n" + JSONUtil.parseArray(LISTENINGGROUPIDSET).toStringPretty());
        if (json.isEmpty()) {
            log.info("打印监听群聊的集合-失败!");
            return;
        }
        log.info("打印监听群聊的集合-成功!");
    }


    /**
     * 往监听群聊的对象
     * 请确保机器人确实在该群
     *
     * @param group_id 群号,请确保机器人所在该群
     */
    public static void addlisteninggroupGather(String group_id) {
        if (!(LISTENINGGROUPIDSET.add(group_id))) {
            log.info("往监听群聊的对象-失败!,该群已经在监听集合里了");
            SignalUtil.sendGroupMessage(group_id, "添加失败!,该群已经在监听集合里了!");
            return;
        }
        SignalUtil.sendGroupMessage(group_id, "已添加标记当前群为监听对象!");
    }

    /**
     * 删除监听群聊集合里指定的群聊对象
     *
     * @param group_id
     */
    public static void removelisteninggroupGather(String group_id) {
        if (!(LISTENINGGROUPIDSET.remove(group_id))) {
            log.info("删除监听群聊集合里指定的群聊对象-失败!,该群并不在监听群聊集合里");
            SignalUtil.sendGroupMessage(group_id, "删除失败,该群并不在监听群聊集合里");
            return;
        }
        SignalUtil.sendGroupMessage(group_id, "已移除取消标记当前群聊的监听状态!");
    }


    /**
     * 添加标记指定群聊作为推送群聊
     * 添加到推送群聊集合对象
     *
     * @param group_id 群号
     */
    public static void addPoshGroupGather(String group_id) {
        boolean add = PUSHGROUPIDSET.add(group_id);
        if (!add) {
            log.info("添加标记指定群聊作为推送群聊-添加失败!");
            SignalUtil.sendGroupMessage(group_id, "标记当前群为推送群聊失败,已经添加过了!");
            return;
        }
        log.info("添加标记指定群聊作为推送群聊-添加成功!");
        SignalUtil.sendGroupMessage(group_id, "已标记当前群为推送群聊");
    }

    /**
     * 取消标记指定群聊作为推送群聊
     * 从推送集合对象里移除
     *
     * @param group_id
     */
    public static void removePoshGroupGather(String group_id) {
        if (!(PUSHGROUPIDSET.remove(group_id))) {
            log.info("取消标记指定群聊作为推送群聊-失败!");
            SignalUtil.sendGroupMessage(group_id, "取消标记当前群为推送群聊失败,当前群并不在推送集合内");
            return;
        }
        log.info("取消标记指定群聊作为推送群聊-成功!");
        SignalUtil.sendGroupMessage(group_id, "取消标记当前群为推送群聊成功");

    }


}
