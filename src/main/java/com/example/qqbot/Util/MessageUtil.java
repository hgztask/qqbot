package com.example.qqbot.Util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public static @NonNull List<JSONObject> getTypeList(@NonNull String type, @NonNull JSONArray jsonArray) {
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
    public static List<JSONObject> getTypeImageList(@NonNull JSONArray jsonArray) {
        return MessageUtil.getTypeList("image", jsonArray);
    }


    /**
     * 获取消息列表中的所有的图片直链
     *
     * @param jsonArray 消息列表
     * @return 图片列表
     */
    public static Set<String> getTypeImageURLList(@NonNull JSONArray jsonArray) {
        List<JSONObject> messageTypeImageList = MessageUtil.getTypeImageList(jsonArray);
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
     * 获取消息列表中的所有的图片直链
     *
     * @param typeImageList 对应已经后去消息中指定类型或者手游类型得List列表
     * @return 图片直链集合
     */
    public static Set<String> getTypeImageURLList(List<JSONObject> typeImageList) {
        HashSet<String> set = new HashSet<>();
        if (!(isTypeImage(typeImageList))) {
            return set;
        }
        for (JSONObject entries : typeImageList) {
            String url = entries.getByPath("data.url", String.class);
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
    public static String getReplyID(JSONArray message) {
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
    public static List<String> getTypeList(JSONArray messageJsonArray) {
        return messageJsonArray.getByPath("type", List.class);
    }

    /**
     * 判断消息是否是语音类型
     *
     * @param messageJsonArray json消息内容中的类型列表
     * @return 是否是
     */
    public static boolean isTypeRecord(JSONArray messageJsonArray) {
        List<String> messageTypeList = getTypeList(messageJsonArray);
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
    public static @NonNull List<JSONObject> getTypeByAtList(@NonNull JSONArray jsonArray) {
        return MessageUtil.getTypeList("at", jsonArray);
    }

    /**
     * 获取消息中的第一个at成员qq号码
     *
     * @param jsonArray json消息内容
     * @return qq群员号码
     */
    public static String getOneAtID(JSONArray jsonArray) {
        JSONObject jsonObject;
        try {
            jsonObject = getTypeByAtList(jsonArray).get(0);
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
     * @param type       指定类型
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
     * @param typeImageList 消息类型列表
     * @return 是否有
     */
    public static boolean isTypeImage(List<JSONObject> typeImageList) {
        for (JSONObject jsonObject : typeImageList) {
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


    /**
     * 返回时间路径
     *
     * @return 字符串
     */
    private static String getTimePathImage() {
        DateTime date = DateUtil.date();
        String year = DateUtil.format(date, "yyyy");
        String month = DateUtil.format(date, "MM");
        String day = DateUtil.format(date, "dd");
        String h = DateUtil.format(date, "HH");
        return String.format("\\%s年\\%s月\\%s日\\%s时\\", year, month, day, h);
    }


    /**
     * 下载文件
     *
     * @param url      图片直链接
     * @param path     存储路径
     * @param fileNmae 文件名
     */
    public static void downloadFIle(String url, String path, String fileNmae) {
        HttpUtil.downloadFile(url, path + fileNmae);
    }

    /**
     * 针对群聊的保存图片功能
     * 该方法可以指定文件名
     *
     * @param group_id 群号
     * @param user_id  用户
     * @param url      图片直链
     * @param fileName 文件名
     */
    public static void downloadGroupImage(String group_id, String user_id, String url, String fileName) {
        downloadFIle(url, "E:\\qqbot\\群聊图片" + getTimePathImage() + group_id + "\\" + user_id + "\\", fileName);
    }


    /**
     * 针对群聊的保存图片功能
     *
     * @param jsonObject json图片样式
     * @param group_id   群号
     * @param user_id    qq号
     */
    public static void downloadGroupImage(@NonNull JSONObject jsonObject, String group_id, String user_id) {
        String file = jsonObject.getByPath("data.file", String.class);
        String url = jsonObject.getByPath("data.url", String.class);
        downloadGroupImage(group_id, user_id, url, file.replace(".image", ".jpg"));
    }

    /**
     * 针对群聊的保存图片功能
     * 多线程执行,相应快
     *
     * @param list     json图片样式List集合
     * @param group_id 群号
     * @param user_id  QQ号
     */
    public static void downloadGroupImageThread(List<JSONObject> list, String group_id, String user_id) {
        @SuppressWarnings("all")
        ExecutorService threadPool = Executors.newFixedThreadPool(list.size());
        try {
            for (JSONObject entries : list) {
                threadPool.execute(() -> downloadGroupImage(entries, group_id, user_id));
            }
        } finally {
            threadPool.shutdown();
        }
    }

    /**
     * 针对群聊撤回时保存图片功能
     *
     * @param url
     * @param group
     * @param user_id
     * @param fileName 文件名
     */
    public static void downloadGroupRecallImage(String url, String group, String user_id, String fileName) {
        downloadFIle(url, "E:\\qqbot\\群聊撤回图片" + getTimePathImage() + group + "\\" + user_id + "\\", fileName);
    }

    /**
     * 针对群聊的撤回保存图片功能
     * 单个图片样式
     *
     * @param jsonObject json图片样式
     * @param group_id   群号
     * @param user_id    qq号
     */
    public static void downloadGroupRecallImage(@NonNull JSONObject jsonObject, String group_id, String user_id) {
        String file = jsonObject.getByPath("data.file", String.class);
        String url = jsonObject.getByPath("data.url", String.class);
        downloadGroupRecallImage(group_id, user_id, url, file.replace(".image", ".jpg"));
    }

    /**
     * 针对群聊撤回时保存图片功能
     * 多线程执行
     *
     * @param list     图片直链的集合
     * @param group_id 群聊号
     * @param user_id  用户
     */
    public static void downloadGroupRecallImageThread(List<JSONObject> list, String group_id, String user_id) {
        @SuppressWarnings("all")
        ExecutorService threadPool = Executors.newFixedThreadPool(list.size());
        try {
            for (JSONObject v : list) {
                threadPool.execute(() -> downloadGroupRecallImage(v, group_id, user_id));
            }
        } finally {
            threadPool.shutdown();
        }
    }


}
