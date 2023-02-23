package com.example.qqbot.model.group;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataPoke;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 机器人被戳逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/22 23:15
 */
@Slf4j
@Component
public class GroupBotStamp implements Runnable, IMessageEvent {

    private DataPoke dataPoke;

    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 1;
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
        if (!("poke".equals(message.getSub_type()))) {
            return false;
        }
        //如果是戳一戳类型
        if (message.getGroup_id() == null) {
            //等于空说明不是群戳一戳,反之是好友戳一戳!
            return false;
        }
        this.dataPoke = JSONUtil.toBean(jsonObject, DataPoke.class);
        //被戳者 QQ 号
        String target_id = dataPoke.getTarget_id();
        if (!(DataUserEights.BOTUSERID.contains(target_id))) {
            return false;
        }
        //当被戳的一方是机器人时
        this.run();
        return true;
    }


    @Override
    public void run() {
        String group_id = dataPoke.getGroup_id();
        //主动戳的一方
        String user_id = dataPoke.getUser_id();
        SignalUtil.sendGroupMessage(group_id, "别戳了,再戳头要秃了");
    }
}
