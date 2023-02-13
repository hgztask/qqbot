package com.example.qqbot.model.group;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.model.ReReadingModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
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

    /**
     * 黑名单群组
     */
    private static List<String> BLACKGROUPID = ListUtil.toList("");

    /**
     * 黑名单用户
     * 该用户的信息不会推送
     */
    private static final List<String> BLACKUSERID = ListUtil.toList("2978778354", "3426359078", "202619505");

    /**
     * 一个群重复次数
     */
    private static int groupEqualIndex = 1;

    /**
     * 记录重复值
     */
    private static String groupEqualStr = "";


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
        String user_id = dataGroup.getUser_id();
        String group_id = dataGroup.getGroup_id();
        //判断是否有黑名单用户和黑名单群聊
        if (BLACKUSERID.contains(user_id) || BLACKGROUPID.contains(group_id)) {
            //包含在内的是不会执行推送消息的
            log.info("触发了黑名单了操作了:ser_id=" + user_id + "\tgroup_id=" + group_id);
            return;
        }


        if (isequlContent(dataGroup.getRaw_message())) {
            //当连续有两条消息重复时不执行下面操作
            return;
        }


        log.info("捕获到信息=" + JSONUtil.parseObj(dataGroup).toStringPretty());

        for (String pushGroupID : PUSHGROUPIDSET) {
            if (SignalUtil.sendGroupMessage(pushGroupID, dataGroup.getRaw_message()).isEmpty()) {
                log.info("监听群聊推送消息失败!");
                return;
            }
            log.info("监听群聊推送消息成功!");
        }
    }

    /**
     * 判断上一条消息是否和下一条消息一样,一样就返回true
     * 反之false
     * 该方法需要加锁,避免多条线程同时进行判断!
     *
     * @param raw_message 原始消息内容
     * @return 是否一样布尔值
     */
    private synchronized static boolean isequlContent(String raw_message) {
        //这里执行群聊消息判断与上一次内容是否相等
        if (groupEqualStr.equals(raw_message)) {
            groupEqualIndex++;
            //如果相容的内容=>1次就不推送
            if (groupEqualIndex >= 2) {
                log.info("检测到连续消息大于或等于2次,故不推送消息!");
                return true;
            }//反之说明消息才出现1次
        }
        //不相等就记录对应的消息,也就是每次都会刷刷新上一条消息和刷新重复次数1次,已保证下次和上次的判断
        groupEqualStr = raw_message;
        groupEqualIndex = 1;
        return false;
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






