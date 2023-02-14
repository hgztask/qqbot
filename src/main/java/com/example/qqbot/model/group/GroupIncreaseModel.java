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
 * 群人数增加逻辑层,即新人进群逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 16:54
 */
@Slf4j
@Component
public class GroupIncreaseModel implements Runnable, GetTypeFace, IMessageEvent {
    /**
     * 群人数增加的数据对象
     */
    private DataGroupDecrease dataGroupDecrease;

    @Override
    public void run() {
        /**
         * 先定义相关变量,后面拓展需求时再利用
         */

        HashMap<String, String> data = new HashMap<>();
        //进群的QQ号
        String user_id = dataGroupDecrease.getUser_id();
        //所在的群号
        String group_id = dataGroupDecrease.getGroup_id();
        //操作者 QQ 号 ( 如果是主动退群, 则和 user_id 相同 )
        String operator_id = dataGroupDecrease.getOperator_id();
        //有人进群则分别表示 approve 管理员已同意入群、 invite 管理员邀请入群
        String sub_type = dataGroupDecrease.getSub_type();
        long time = dataGroupDecrease.getTime() * 1000;
        String message = CharSequenceUtil.format("[CQ:image,file=头像,url=https://q1.qlogo.cn/g?b=qq&nk={}&s=640]\n" +
                        "欢迎[CQ:at,qq={}]进群\n" +
                        "操作对象:{}\n" +
                        "事件类型:{}\n" +
                        "时间:{}",
                user_id, user_id, operator_id, getType(sub_type), DateUtil.date(time));
        SignalUtil.sendGroupMessage(group_id, message);

    }

    /**
     * 根据内容返回指定类型的字符串值
     *
     * @param content 内容
     * @return 类型
     */
    @Override
    public String getType(String content) {
        return switch (content) {
            case "approve" -> "管理员已同意入群";
            case "invite" -> "管理员邀请入群";
            default -> "w未知状态";
        };
    }

    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 0;
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
        if (!("group_increase".equals(message.getNotice_type()))) {
            return false;
        }
        //群成员增加
        this.dataGroupDecrease = BeanUtil.toBean(jsonObject, DataGroupDecrease.class);
        log.info(dataGroupDecrease.getGroup_id() + "群的 " + dataGroupDecrease.getUser_id() + " 成员增加了");
        this.run();
        return true;
    }
}

