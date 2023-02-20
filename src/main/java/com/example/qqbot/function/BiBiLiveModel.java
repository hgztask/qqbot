package com.example.qqbot.function;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
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

import java.util.*;

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
    private DataGroup dataGroup;


    /**
     * 每个群聊都可以指定自身要监听哪个房间号,且每个群聊之间独立
     * 格式
     * 群聊:直播间房间号id
     */
    private static final Map<String, Set<String>> GROUP_ROOMID = new HashMap<>();

    /**
     * 对应房间号存储的聊天对象
     * 房间号:聊天内容对象
     */
    private static final Map<String, Set<DataBiBiRoomLive>> ROOM_MESSAGE_SET = new HashMap<>();


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
        if (GROUP_ROOMID.isEmpty()) {
            return false;
        }
        if (GROUP_ROOMID.keySet().contains(group_id)) {
            //有该群时,对该群id进行追加
            if (GROUP_ROOMID.get(group_id).add(roomID)) {
                return true;
            }
            return false;
        }
        //当集合里没有该群聊时,添加初始化
        HashSet<String> set = new HashSet<>();
        set.add(roomID);
        GROUP_ROOMID.put(group_id, set);
        return true;
    }


    /**
     * 权重,权重高的值会先匹配
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
        this.dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
        String user_id = dataGroup.getUser_id();
        String group_id = dataGroup.getGroup_id();
        String raw_message = dataGroup.getRaw_message();
        boolean boolUser = DataUserEights.SUPERUSER.contains(user_id);
        if (raw_message.startsWith("设置监听指定b站直播间弹幕=") && boolUser) {
            String roomId = InformationUtil.subEqual("=", raw_message);
            if (roomId.isEmpty()) {
                return false;
            }
            try {
                Integer.valueOf(roomId);
            } catch (NumberFormatException e) {
                return false;
            }
            if (this.addGroupID(group_id, roomId)) {
                log.info("添加该房间号成功!" + roomId);
                return true;
            }
            log.info("添加该房间号失败!" + roomId);
            return true;
        }

        if (GROUP_ROOMID.isEmpty()) {
            return false;
        }
        if (GROUP_ROOMID.values().isEmpty()) {
            return false;
        }


        return false;
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
     * 执行遍历集合里房间号进行更新
     */
    @Override
    public void run() {

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
            //对有关联的群聊进行推送消息!
            for (String groupid : groupIdSet) {
                for (DataBiBiRoomLive v : subRoomLives) {
                    //有多少消息就对其对应群聊发送多少条消息;
                    SignalUtil.sendGroupMessage(groupid, printFormat(v));
                }
            }
        }


    }
}
