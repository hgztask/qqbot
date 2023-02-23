package com.example.qqbot.data.json;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.NonNull;

import java.util.Collection;

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
     * json样式文本
     *
     * @param text 显示的文本内容
     * @return
     */
    public static JSONObject text(StringBuilder text) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("type", "text");
        jsonObject.set("data", new JSONObject().set("text", text));
        return jsonObject;
    }

    /**
     * 回复引用消息
     *
     * @param message_id 消息id
     * @return jsonobj对象
     */
    public static JSONObject reply(String message_id) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        data.set("id", message_id);
        jsonObject.set("type", "reply");
        jsonObject.set("data", data);
        return jsonObject;
    }

    /**
     * 指定引用消息回复,简而言之,就是回复别人
     * 该方法封装了别的json对象,但是只能回复文本内容
     *
     * @param message_id 引用的消息id
     * @param user_id    回复的对象,也就是要艾特谁
     * @param content    回复的内容
     * @return jsonarry对象
     */
    public static JSONArray reply(String message_id, String user_id, String content) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(DataJson.reply(message_id));
        jsonArray.add(DataJson.at(user_id));
        jsonArray.add(text(content));
        return jsonArray;
    }

    /**
     * 生成json样式的at对象
     *
     * @param user_id @的 QQ 号, all 表示全体成员
     * @param name    当在群中找不到此QQ号的名称时才会生效
     * @return json样式对象
     */
    public static JSONObject at(String user_id, String name) {
        JSONObject jsonObject = new JSONObject(2);
        JSONObject data = new JSONObject(2);
        jsonObject.set("type", "at");
        jsonObject.set("data", data);
        data.set("qq", user_id);
        data.set("name", name);
        return jsonObject;
    }

    /**
     * 生成json样式的at对象
     *
     * @param user_id @的 QQ 号, all 表示全体成员
     * @return json样式对象
     */
    public static JSONObject at(String user_id) {
        return at(user_id, "艾特无效.此人不在群里");
    }


    /**
     * 封装json样式的image类型消息
     * 只能发送网络图片
     *
     * @param file  文件名
     * @param url   网络地址
     * @param cache 只在通过网络 URL 发送时有效, 表示是否使用已缓存的文件, 默认为1,即默认true
     * @return jsonObj对象
     */
    public static JSONObject imageUrl(String file, String url, boolean cache) {
        JSONObject jsonObject = new JSONObject(2);
        JSONObject data = new JSONObject(3);
        jsonObject.set("type", "image");
        jsonObject.set("data", data);
        data.set("file", file);
        data.set("url", url);
        data.set("cache", cache ? 1 : 0);
        return jsonObject;
    }

    /**
     * 封装json样式的image类型消息
     * 只能发送本地图片或者是缓存的图片文件如 8f538f3a4d55097bae7ad559aab40337.image 等
     *
     * @param file 本地绝对路径
     * @return
     */
    public static JSONObject imageFile(String file) {
        JSONObject imageFIle = new JSONObject(2);
        imageFIle.set("type", "image");
        JSONObject data = new JSONObject(1);
        imageFIle.set("data", data);
        data.set("file", "file:///"+file);
        return imageFIle;
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
     * 合并消息成合并聊天记录
     *
     * @param name       显示昵称
     * @param user_id    对应的QQ号,可以不填写真实的,或者任意数字
     * @param collection 消息的内容列表
     * @return
     */
    public static JSONArray nodeMerge(String name, String user_id, Collection<?> collection) {
        JSONArray jsonArray = new JSONArray(collection.size());
        for (Object o : collection) {
            JSONObject node = DataJson.node();
            JSONObject nodeData = DataJson.nodeData(name, user_id);
            node.set("data", nodeData);
            nodeData.set("content", o);
            jsonArray.add(node);
        }
        return jsonArray;
    }


    /**
     * 使用json样式集合的元素编写一条消息的内容,比如艾特人之后,消息后面跟上文本回复,再跟上表钱包等操作
     *
     * @param collection json样式集合
     * @return jsonjieguo
     */
    public static JSONArray text(@NonNull Collection<?> collection) {
        JSONArray jsonArray = new JSONArray(collection.size());
        jsonArray.addAll(collection);
        return jsonArray;
    }


}
