package com.example.qqbot.Util;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 消息处理
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/22 12:27
 */
@Slf4j
public class MessageUtil {

    /**
     * 获取消息中的所有指定类型元素对象集合
     *
     * @param type      指定类型
     * @param jsonArray messageJson对象
     * @return 对应类型元素集合
     */
    public static @NonNull List<JSONObject> getMessageTypeList(@NonNull String type, @NonNull JSONArray jsonArray) {
        ArrayList<JSONObject> arrayList = new ArrayList<>();
        List<String> typeList = jsonArray.getByPath("type", List.class);
        if (typeList == null || typeList.isEmpty()) {
            return arrayList;
        }
        for (int i = 0; i < typeList.size(); i++) {
            if (!(type.equals(typeList.get(i)))) {
                continue;
            }
            arrayList.add(JSONUtil.parseObj(jsonArray.get(i)));
        }
        return arrayList;
    }


    /**
     * 获取json消息中的所有图片列表
     *
     * @param jsonArray 消息列表
     * @return 图片列表
     */
    public static List<JSONObject> getMessageTypeImageList(@NonNull JSONArray jsonArray) {
        return MessageUtil.getMessageTypeList("image", jsonArray);
    }


    /**
     * 获取消息列表中的所有的图片直链
     *
     * @param jsonArray 消息列表
     * @return 图片列表
     */
    public static Set<String> getMessageImageURLList(@NonNull JSONArray jsonArray) {
        List<JSONObject> messageTypeImageList = MessageUtil.getMessageTypeImageList(jsonArray);
        Set<String> set = new LinkedHashSet<>();
        for (JSONObject jsonObject : messageTypeImageList) {
            String url = jsonObject.getByPath("data.url", String.class);
            if (url == null) {
                continue;
            }
            set.add(url);
        }
        return set;
    }

    /**
     * 回复时所引用的消息id
     *
     * @param message json消息内容
     */
    public static String getMessageReplyID(JSONArray message) {
        String byPath = message.getByPath("[0].type", String.class);
        if (byPath == null || byPath.isEmpty()) {
            log.info("获取失败!");
            return "";
        }
        if (!("reply".equals(byPath))) {
            return "";
        }
        String id = message.getByPath("[0].data.id", String.class);
        if (id == null || id.isEmpty()) {
            return "";
        }
        return id;
    }



    /**
     * 获取中消息内容中的json类型的类型列表
     *
     * @param messageJsonArray 消息内容(非原始内容,而是json)
     * @return 类型列表
     */
    public static List<String> getMessageTypeList(JSONArray messageJsonArray) {
        return messageJsonArray.getByPath("type", List.class);
    }

    /**
     * 判断消息是否是语音类型
     *
     * @param messageJsonArray json消息内容中的类型列表
     * @return 是否是
     */
    public static boolean isMessageTypeRecord(JSONArray messageJsonArray) {
        List<String> messageTypeList = getMessageTypeList(messageJsonArray);
        for (String type : messageTypeList) {
            if ("record".equals(type)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取消息中的所有艾特成员
     *
     * @param jsonArray json消息内容
     * @return 应类型元素集合
     */
    public static @NonNull List<JSONObject> getMessageTypeByAtList(@NonNull JSONArray jsonArray) {
        return MessageUtil.getMessageTypeList("at", jsonArray);
    }

    /**
     * 获取消息中的第一个at成员qq号码
     *
     * @param jsonArray json消息内容
     * @return qq群员号码
     */
    public static String getMessageOneAtID(JSONArray jsonArray) {
        JSONObject jsonObject;
        try {
            jsonObject = getMessageTypeByAtList(jsonArray).get(0);
        } catch (Exception e) {
            log.info("getMessageOneAtID出错了" + e.getMessage());
            return "";
        }
        if (jsonObject.isEmpty()) {
            return "";
        }
        String atID = jsonObject.getByPath("data.qq", String.class);
        if (atID == null) {
            return "";
        }
        return atID;
    }



    /**
     * 获取中消息内容中的json类型的值的类型列表
     *
     * @param messageJsonArray 消息内容(非原始内容,而是json)
     * @return 类型列表
     */
    public static List<String> geTypeValueList(JSONArray messageJsonArray) {
        return messageJsonArray.getByPath("type", List.class);
    }

    /**
     * 判断单个消息类型列表中是否有指定类型元素
     *
     * @param type 指定类型
     * @param jsonObject 消息类型元素
     * @return 是否是
     */
    public static boolean isTypeValue(@NonNull String type, JSONObject jsonObject) {
        String temp = jsonObject.getByPath("type", String.class);
        if (temp == null || temp.isEmpty()) {
            return false;
        }
        if (type.equals(temp)) {
            return true;
        }
        return false;
    }


    /**
     * 判断json消息中是否有图片类型的元素
     *
     * @param jsonArray 消息列表
     * @return 是否有
     */
    public static boolean isTypeValueImage(JSONArray jsonArray) {
        List<String> messageTypeList = geTypeValueList(jsonArray);
        for (String v : messageTypeList) {
            if ("image".equals(v)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断消息类型列表中是否有图片类型元素
     * 该方法是针对于获取过元素类型的列表,不支持未获取的过元素对内的列表
     *
     * @param jsonObjectList 消息类型列表
     * @return 是否有
     */
    public static boolean isTypeImage(List<JSONObject> jsonObjectList) {
        for (JSONObject jsonObject : jsonObjectList) {
            return isTypeImage(jsonObject);
        }
        return false;
    }

    /**
     * 判断单个消息类型列表中是否有图片类型元素
     *
     * @param jsonObject 消息类型元素
     * @return 是否是
     */
    public static boolean isTypeImage(JSONObject jsonObject) {
        return isTypeValue("image", jsonObject);
    }



}
