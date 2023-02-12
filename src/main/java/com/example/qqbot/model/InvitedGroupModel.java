package com.example.qqbot.model;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.example.qqbot.SignalUtil;
import com.example.qqbot.data.group.DataInvitedGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理加群请求/邀请的逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/12 11:22
 */
@Slf4j
public class InvitedGroupModel implements Runnable {

    private DataInvitedGroup dataInvitedGroup;

    public InvitedGroupModel(DataInvitedGroup dataInvitedGroup) {
        this.dataInvitedGroup = dataInvitedGroup;
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        JSONObject groupInfo = SignalUtil.getGroupInfo(dataInvitedGroup.getGroup_id(), false);
        if (groupInfo.isEmpty()) {
            log.info("获取群信息失败了!");
            return;
        }
        //群人数上限
        Integer max_member_count = groupInfo.getByPath("data.max_member_count", int.class);
        //目前人数多少
        Integer member_count = groupInfo.getByPath("data.member_count", int.class);
        long time = dataInvitedGroup.getTime() * 1000;
        InvitedGroupModel.printGroupInvited(dataInvitedGroup, time);
        //处理战双帕弥什-桐子酱的光剑公会群(一群)
        if ("760849278".equals(dataInvitedGroup.getGroup_id())) {
            manchusAndImans(max_member_count, member_count, dataInvitedGroup, "本群已满，请进二群689219358");
        } else {
            //非上述群聊时候
            manchusAndImans(max_member_count, member_count, dataInvitedGroup, "null");
        }
    }


    /**
     * 处理群满或未满的情况
     */
    private static void manchusAndImans(Integer max_member_count, Integer member_count, DataInvitedGroup dataInvitedGroup, String reason) {
        //群满时
        //目前已知群满的逻辑有问题,满了依旧不会执行通过对应成员,故后期如果有机会群满之后记得调试测试
        if (max_member_count.equals(member_count)) {
            if (SignalUtil.setGroupAddRequest(dataInvitedGroup.getFlag(), dataInvitedGroup.getSub_type(), false, reason).isEmpty()) {
                log.info("处理加群/邀请失败!");
                return;
            }
            log.info("处理加群请求/邀请的成功!");
            return;
        }
        //群未满的情况下
        if (SignalUtil.setGroupAddRequest(dataInvitedGroup.getFlag(), dataInvitedGroup.getSub_type(), true, "null").isEmpty()) {
            log.info("处理加群/邀请失败!");
            return;
        }
        log.info("处理加群请求/邀请的成功!");
        return;
    }


    /**
     * 在群里通知入群通知信息
     *
     * @param dataInvitedGroup 加群请求／邀请的数据层
     * @param time             时间戳
     */
    @SuppressWarnings("all")
    private static void printGroupInvited(DataInvitedGroup dataInvitedGroup, long time) {
        if (SignalUtil.sendGroupMessage(dataInvitedGroup.getGroup_id(), String.format("群通知:申请进群成员%s\n验证信息(进群理由):%s\n时间:%s",
                dataInvitedGroup.getUser_id(), dataInvitedGroup.getComment(), DateUtil.date(time))).isEmpty()) {
            log.info("处理加群请求/邀请-同意入群之后发送群消息-失败!");
            return;
        }
        log.info("处理加群请求/邀请-同意入群之后发送群消息-成功!");
    }


}





