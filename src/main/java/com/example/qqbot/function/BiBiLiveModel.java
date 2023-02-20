package com.example.qqbot.function;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.NetworkUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.bilibili.DataBiBiRoomLive;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.json.DataJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 实现监听b站指定用户直播间弹幕
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/20 13:26
 */
@Slf4j
@Component
public class BiBiLiveModel implements Runnable, IMessageEvent {

    /**
     * b站直播间监听弹幕
     */
    private static final File FIlE_GROUP_ROOMID = new File("E:\\qqbot\\b站直播房间号.json");

    /**
     * b站直播间弹幕内容
     */
    private static final File FIlE_ROOM_MESSAGE = new File("E:\\qqbot\\b站直播间弹幕内容.json");


    /**
     * 每个群聊都可以指定自身要监听哪个房间号,且每个群聊之间独立
     * 格式
     * 群聊:直播间房间号id
     */
    @SuppressWarnings("all")
    private static Map<String, Set<String>> GROUP_ROOMID = getFilegroupRoomidjson(FIlE_GROUP_ROOMID);

    /**
     * 对应房间号存储的聊天对象
     * 房间号:聊天内容对象
     */
    private static final Map<String, Set<DataBiBiRoomLive>> ROOM_MESSAGE_SET = getFileRoomJson(FIlE_ROOM_MESSAGE);

    /**
     * 对应群聊是否开始监听
     */
    private static final Map<String, Boolean> GROUPLINST = new HashMap<>();


    /**
     * 保存当前群聊与房间号的关联
     *
     * @return 是否保存成功
     */
    private static boolean writeGROUP_ROOMED() {
        if (FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(GROUP_ROOMID), FIlE_GROUP_ROOMID).isFile()) {
            return true;
        }
        return false;
    }


    /**
     * 获取本地b站直播间监听弹幕数据
     *
     * @return
     */
    @SuppressWarnings("all")
    private static Map<String, Set<String>> getFilegroupRoomidjson(File filePath) {
        Map<String, Set<String>> map = new HashMap<>();
        JSONObject temp;
        try {
            temp = JSONUtil.readJSONObject(filePath, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new HashMap();
        }
        for (String s : temp.keySet()) {
            map.put(s, temp.get(s, Set.class));
        }
        return map;
    }

    /**
     * 获取本地b站直播间弹幕数据
     *
     * @return
     */
    @SuppressWarnings("all")
    private static Map<String, Set<DataBiBiRoomLive>> getFileRoomJson(File filePath) {
        Map<String, Set<DataBiBiRoomLive>> map = new HashMap();
        JSONObject temp;
        try {
            temp = JSONUtil.readJSONObject(filePath, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return map;
        }
        for (String s : temp.keySet()) {
            map.put(s, temp.get(s, Set.class));
        }
        return map;
    }


    /**
     * 针对群聊进行添加指定房间号
     * 如果没有对应群聊则新建一个元素进行并对该元素进行添加房间号
     * 反之
     *
     * @param group_id 群聊
     * @param roomID   房间号
     * @return 是否成功
     */
    private boolean addGroupID(String group_id, String roomID) {
        if (GROUP_ROOMID.containsKey(group_id)) {
            //有该群记录时
            Set<String> set = GROUP_ROOMID.get(group_id);
            if (set.contains(roomID)) {
                return false;
            }
            //该群没有该房间号,则添加
            return set.add(roomID);
        }
        //没有该群记录时
        //创建新的set集合用于存储对应房间号
        Set<String> set = new HashSet<>();
        GROUP_ROOMID.put(group_id, set);
        return set.add(roomID);
    }


    /**
     * 对房间号集合添加房间号与set集合
     *
     * @param roomid 房间号
     */
    private boolean addRoomID(String roomid) {
        if (ROOM_MESSAGE_SET.containsKey(roomid)) {
            return false;
        }
        ROOM_MESSAGE_SET.put(roomid, new HashSet<>());
        return true;
    }


    /**
     * 通过房间号id找到对应与其有关联的群聊id
     *
     * @param roomid 房间号id
     * @return 有关联的群聊id
     */
    private Set<String> getGroupId(String roomid) {
        Set<String> groupSet = new HashSet<>();
        for (String groupId : GROUP_ROOMID.keySet()) {
            //对群聊id遍历
            if (GROUP_ROOMID.get(groupId).contains(roomid)) {
                //针对对应的群聊id的set集合是否有该房间号,有就存入对应群聊id到groupSet集合中
                //这个群聊如果有该房间号
                groupSet.add(groupId);
            }
        }
        return groupSet;
    }


    /**
     * 打印指定群聊所添加的房间号
     *
     * @param group_id 群号
     */
    private void printGroupRoomId(String group_id) {
        Set<String> set = GROUP_ROOMID.get(group_id);
        SignalUtil.sendGroupMessage(group_id, "当前群聊监听的房间号\n" + JSONUtil.toJsonPrettyStr(set));
    }


    /**
     * 对单个弹幕消息封装
     *
     * @param dataBiBiRoomLive 弹幕消息对象
     * @return 封装好之后的字符串内容
     */
    private String printFormat(DataBiBiRoomLive dataBiBiRoomLive) {
        String format = CharSequenceUtil.format("""
                        用户名(uid):{}-({})
                        发送时间:{}
                        发送内容:{}
                        """, dataBiBiRoomLive.getNickname(), dataBiBiRoomLive.getUid(),
                dataBiBiRoomLive.getTimeline(), dataBiBiRoomLive.getText());
        return format;
    }

    /**
     * 对消息内容进行格式化输出并封装号消息对象组,只需要调用对应的发消息方法即可
     *
     * @param subRoomLives 新的聊天对象
     * @return 封装好的消息内容对象
     */
    private JSONArray printFormats(Collection<DataBiBiRoomLive> subRoomLives) {
        JSONArray item = new JSONArray();
        for (DataBiBiRoomLive v : subRoomLives) {
            item.add(DataJson.text(printFormat(v)));
        }
        return item;
    }


    /**
     * 保存对应房间号存储的聊天对象 房间号:聊天内容对象
     *
     * @return
     */
    private synchronized static boolean whireRoomMessage() {
        return FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(ROOM_MESSAGE_SET), FIlE_ROOM_MESSAGE).isFile();
    }

    private boolean clearRoomMessage(String room_id) {
        if (!(ROOM_MESSAGE_SET.containsKey(room_id))) {
            return false;
        }
        ROOM_MESSAGE_SET.get(room_id).clear();
        return true;
    }


    /**
     * 权重,权重高的值会先匹配
     * 要求比群聊高
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 39;
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
        DataGroup dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
        String user_id = dataGroup.getUser_id();
        String self_id = dataGroup.getSelf_id();
        String group_id = dataGroup.getGroup_id();
        String raw_message = dataGroup.getRaw_message();
        boolean boolUser = DataUserEights.SUPERUSER.contains(user_id);
        if (raw_message.startsWith("设置监听指定b站直播间弹幕=") && boolUser) {
            String roomId = InformationUtil.subEqual("=", raw_message);
            if (roomId.isEmpty()) {
                return true;
            }
            try {
                Integer.valueOf(roomId);
            } catch (NumberFormatException e) {
                return true;
            }
            addRoomID(roomId);
            if (this.addGroupID(group_id, roomId)) {
                SignalUtil.sendGroupMessage(group_id, "添加该房间号成功!" + roomId);
                return true;
            }
            SignalUtil.sendGroupMessage(group_id, "添加该房间号失败!" + roomId);
            return true;
        }
        if (raw_message.startsWith("打印指定b站直播间弹幕=") && boolUser) {
            String roomId = InformationUtil.subEqual("=", raw_message);
            if (roomId.isEmpty()) {
                return true;
            }
            Set<DataBiBiRoomLive> roomLives = NetworkUtil.getBiBiliveMessage(roomId);
            if (roomLives.isEmpty()) {
                return true;
            }
            if (roomLives.size() == 1) {
                for (DataBiBiRoomLive v : roomLives) {
                    SignalUtil.sendGroupMessage(group_id, printFormat(v));
                }
                return true;
            }
            JSONObject send = SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, printFormats(roomLives)));
            System.out.println(send.toStringPretty());
            return true;
        }
        if (raw_message.startsWith("开启当前群聊所有监听直播间弹幕状态") && boolUser) {
            GROUPLINST.put(group_id, true);
            SignalUtil.sendGroupMessage(group_id, "已设置当前群聊对应的房间号监听状态为true");
            return true;
        }
        if (raw_message.startsWith("关闭当前群聊所有监听直播间弹幕状态") && boolUser) {
            GROUPLINST.put(group_id, false);
            SignalUtil.sendGroupMessage(group_id, "已设置当前群聊对应的房间号监听状态为false");
            return true;
        }
        if (raw_message.startsWith("清空指定直播间弹幕=") && boolUser) {
            String roomId = InformationUtil.subEqual("=", raw_message);
            if (roomId.isEmpty()) {
                return true;
            }
            try {
                Integer.valueOf(roomId);
            } catch (NumberFormatException e) {
                return true;
            }
            if (clearRoomMessage(roomId)) {
                SignalUtil.sendGroupMessage(group_id, "并未有该直播间的弹幕内容");
                return true;
            }
            whireRoomMessage();
            SignalUtil.sendGroupMessage(group_id, "已执行清空,多线程环境下,请注意先暂停再清空");
            return true;
        }

        if (GROUP_ROOMID.isEmpty()) {
            return false;
        }
        if (GROUP_ROOMID.values().isEmpty()) {
            return false;
        }
        if (raw_message.startsWith("保存当前群聊与房间号关联关系") && boolUser) {
            if (writeGROUP_ROOMED()) {
                SignalUtil.sendGroupMessage(group_id, "保存成功!");
                return true;
            }
            SignalUtil.sendGroupMessage(group_id, "保存失败!");
            return true;
        }
        if (raw_message.startsWith("打印当前群聊监听的房间号") && boolUser) {
            Set<String> set = GROUP_ROOMID.get(group_id);
            if (set == null || set.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "当期群聊并未设置相关要监听的房间号!");
                return true;
            }
            printGroupRoomId(group_id);
            return true;
        }
        if (raw_message.startsWith("start监听直播间弹幕") && boolUser) {
            Boolean set = GROUPLINST.get(group_id);
            if (set == null || !(set.equals(true))) {
                SignalUtil.sendGroupMessage(group_id, "未为本群开启监听");
                return true;
            }
            SignalUtil.sendGroupMessage(group_id, "开启监听直播间弹幕...");
            ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
            threadExecutor.execute(() -> start());
            threadExecutor.shutdown();
            log.info("已执行关闭 start监听直播间弹幕 的线程");
            return true;
        }
        return false;
    }

    /**
     * 开始监听对应群聊指定的b站房间号弹幕!
     */
    //@SuppressWarnings("all")
    private void start() {
        Set<String> groupListSet = GROUPLINST.keySet();
        for (String group : groupListSet) {
            while (GROUPLINST.get(group).equals(true)) {
                this.run();
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

    /**
     * 执行遍历集合里房间号进行更新
     */
    @Override
    public synchronized void run() {
        for (String roomid : ROOM_MESSAGE_SET.keySet()) {
            //获取与该房间号有关联的群聊
            Set<String> groupIdSet = getGroupId(roomid);
            if (groupIdSet.isEmpty()) {
                //如果没有关联的结束本轮循环
                log.info("未找到该房间号关联的群聊!roomid=" + roomid);
                continue;
            }
            //拿到在内存的的聊天对象
            Set<DataBiBiRoomLive> roomLives = ROOM_MESSAGE_SET.get(roomid);
            //拿到api中最新的聊天对象
            Set<DataBiBiRoomLive> apiRoomLives = NetworkUtil.getBiBiliveMessage(roomid);
            //一般情况下api的对象比内存的聊天对象多,只需要获取到api中有的,而内存中聊天对象没有的即可,也就是两者的差
            Collection<DataBiBiRoomLive> subRoomLives = CollUtil.subtract(apiRoomLives, roomLives);
            if (subRoomLives.isEmpty()) {
                //说明两者都有的情况
                continue;
            }
            for (String groupid : groupIdSet) {
                //对有关联的群聊进行推送消息!
                Boolean aBoolean = GROUPLINST.get(groupid);
                if (aBoolean == null || !aBoolean) {
                    continue;
                }
                for (DataBiBiRoomLive v : subRoomLives) {
                    if (!(GROUPLINST.get(groupid).equals(true))) {
                        //如果对应群聊关闭了监听状态,也就是不等于true时,就不需要推送消息了
                        continue;
                    }
                    //有多少消息就对其对应群聊发送多少条消息;
                    SignalUtil.sendGroupMessage(groupid, printFormat(v));
                    log.info("已推送对应弹幕到群里");
                    try {//延迟发消息
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            //将api中新的聊天对象插入到内存对应的集合对象
            roomLives.addAll(subRoomLives);
            //保存对应聊天对象
            whireRoomMessage();
        }


    }
}
