package com.example.qqbot.model.group;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.data.DataPoke;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 群戳一戳逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/22 22:49
 */
@Slf4j
@Component
public class GroupStamp implements Runnable, IMessageEvent {

    private DataPoke dataPoke;


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
        if (DataUserEights.BOTUSERID.contains(target_id)) {

        }
        log.info("检测到群戳一戳了!");

        System.out.println(dataPoke.toString());


        return true;
    }

    @Override
    public void run() {

    }
}
