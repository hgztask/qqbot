package com.example.qqbot.data.json;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/15 19:52
 */
public class DataJson {

    /**
     * json样式文本
     *
     * @param text 显示的文本内容
     * @return
     */
    public static JSONObject text(String text) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("type", "text");
        jsonObject.set("data", new JSONObject().set("text", text));
        return jsonObject;
    }

    /**
     * node顶部节点,需要配合nodeData节点使用
     *
     * @return jsonObj对象
     */
    public static JSONObject node() {
        JSONObject node = new JSONObject();
        node.set("type", "node");
        node.set("data", null);
        return node;
    }

    /**
     * nodeData节点,需要配合node节点使用!!!!
     *
     * @param name name消息发送者
     * @param uin  qq号,或者其他
     * @return jsonobj对象
     */
    public static JSONObject nodeData(String name, String uin) {
        JSONObject nodeData = new JSONObject();
        nodeData.set("name", name);
        nodeData.set("uin", uin);
        nodeData.set("content", null);
        return nodeData;
    }


    /**
     * 直接返回一组消息,并合并消息发送到群聊里!
     *
     * @param name      显示昵称
     * @param user_id   对应的QQ号,可以不填写真实的,或者任意数字
     * @param imageList 消息的内容列表
     * @return
     */
    public static JSONArray nodeText(String name, String user_id, List<String> imageList) {
        JSONArray jsonArray = new JSONArray(imageList.size());
        for (String s : imageList) {
            JSONObject node = DataJson.node();
            JSONObject nodeData = DataJson.nodeData(name, user_id);
            node.set("data", nodeData);
            nodeData.set("content", s);
            jsonArray.add(node);
        }
        return jsonArray;
    }




}
