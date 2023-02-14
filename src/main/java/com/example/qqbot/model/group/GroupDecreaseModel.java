package com.example.qqbot.model.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroupDecrease;
import com.example.qqbot.face.GetTypeFace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 群人数减少逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 15:14
 */
@Slf4j
@Component
public class GroupDecreaseModel implements Runnable, GetTypeFace, IMessageEvent {

    /**
     * 群人数减少的数据对象
     */
    private DataGroupDecrease dataGroupDecrease;

    /**
     * 判断群员状态是因为什么原因退的群
     *
     * @param content
     * @return
     */
    @Override
    public final String getType(String content) {
        return switch (content) {
            case "leave" -> "主动退群";
            case "kick" -> "成员被踢";
            case "kick_me" -> "登录号被踢";
            default -> "未知对象";
        };
    }

    /**
     * 获取操作者对象是谁,是用户自己还是管理员等
     *
     * @return 字符串
     */
    private String getOperationObject(String user_id, String operator_id) {
        if (user_id.equals(operator_id)) {
            return "成员自己";
        }
        return operator_id;
    }


    @Override
    public void run() {
        /**
         * 先定义相关变量,后面拓展需求时再利用
         */
        HashMap<String, String> data = new HashMap<>();
        //被踢的QQ号
        String user_id = dataGroupDecrease.getUser_id();
        //所在的群号
        String group_id = dataGroupDecrease.getGroup_id();
        //收到事件的机器人 QQ 号
        String self_id = dataGroupDecrease.getSelf_id();
        //事件子类型, 分别表示主动退群、成员被踢、登录号被踢   leave、kick、kick_me
        String sub_type = dataGroupDecrease.getSub_type();
        //操作者 QQ 号 ( 如果是主动退群, 则和 user_id 相同 )
        String operator_id = dataGroupDecrease.getOperator_id();
        log.info("操作对象:" + operator_id);
        long time = dataGroupDecrease.getTime() * 1000;

        data.put("group_id", dataGroupDecrease.getGroup_id());
        data.put("message", CharSequenceUtil.format("[CQ:image,file=头像,url=https://q1.qlogo.cn/g?b=qq&nk={}&s=640]\n" +
                        "{}成员退出群了\n" +
                        "操作对象:{}\n" +
                        "事件类型:{}\n" +
                        "时间:{}",
                user_id, user_id, getOperationObject(user_id, operator_id), getType(sub_type), DateUtil.date(time)));
        JSONObject json = SignalUtil.httpGet(SignalUtil.getGROUPENDPOINT(), data);
        if (json.isEmpty()) {
            log.info("发送群消息失败了" + json);
            return;
        }
        log.info("发送消息成功!");

    }

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
        if (!("group_decrease".equals(message.getNotice_type()))) {
            return false;
        }
        //群成员减少
        this.dataGroupDecrease = BeanUtil.toBean(jsonObject, DataGroupDecrease.class);
        log.info(dataGroupDecrease.getGroup_id() + "群的 " + dataGroupDecrease.getUser_id() + " 成员减少了");
        this.run();
        return true;
    }
}


