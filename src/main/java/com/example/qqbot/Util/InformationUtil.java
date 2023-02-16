package com.example.qqbot.Util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.data.group.DataGroup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 信息处理
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 22:29
 */
@Slf4j
public class InformationUtil {


    /**
     * 负责获取被艾特方的QQ号码
     *
     * @param jsonObject json对象
     * @return 返回QQ号字符串或者空的字符串
     */
    public static @NonNull String getUserATID(@NonNull JSONObject jsonObject) {
        JSONObject byPath = jsonObject.getByPath("message[1]", JSONObject.class);
        if (byPath == null) {
            return "";
        }
        if (!("at".equals(byPath.get("type", String.class)))) {
            return "";
        }
        String qqIndex = byPath.getByPath("data.qq", String.class);
        if (qqIndex == null || qqIndex.isEmpty()) {
            return "";
        }
        return qqIndex;
    }

    /**
     * 取被艾特方的QQ号码
     *
     * @param dataGroup dataGroup对象
     * @return 返回QQ号字符串或者空的字符串
     */
    public static @NonNull String getUserATID(@NonNull DataGroup dataGroup) {
        return InformationUtil.getUserATID(JSONUtil.parseObj(dataGroup));
    }


    /**
     * 获取指定关键词后面的内容
     * 且关键词是最靠前的那一个
     * 如果截取不到或者就是字符串开头第一个则返回空的字符串
     * 关键词和内容相同会返回空字符串
     * 获取不到会返回空字符串
     *
     * @param key 关键词
     * @param str 字符串
     * @return 截取之后的内容
     */
    public static String subEqual(String key, String str) {
        if (key.equals(str)) {//关键词和内容相同不用截取了
            return "";
        }
        int i = str.indexOf(key);
        if (i == -1) {//获取不到的情况
            return "";
        }

        if (i == 0) {//获取时刚好是前面内容,且要获取关键词后面的内容
            return str.substring(key.length()).trim();
        }
        //正常截取指定关键词后面的内容!
        str = str.substring(i + 1).trim();
        return str;
    }


    /**
     * 获取指定关键词后面的内容
     * 且关键词是最靠前后的那一个
     * 如果截取不到或者就是字符串开头第一个则返回空的字符串
     *
     * @param key 关键词
     * @param str 内容
     * @return 截取之后的内容
     */
    public static String lastSubEqual(String key, String str) {
        int i = str.lastIndexOf(key);
        if (i == -1 || i == 0) {
            return "";
        }
        str = str.substring(i + 1).trim();

        return str;
    }


    /**
     * 获取当前时间的日,比如今天是1月5日,即获取到5
     *
     * @return 日
     */
    public static int getDateDAY_OF_MONTH() {
        return DateUtil.date().getField(DateField.DAY_OF_MONTH);
    }

    /**
     * 遍历集合对象中key与content是否是包含关系
     * 比如:集合内有key=舞台,content=物天空下起下午,舞台开始了,那么key是content的其中的一部分
     *
     * @param list    集合
     * @param content 关键词
     * @return 布尔值
     */
    public static boolean keyContains(Set<String> list, String content) {
        if (content.isEmpty()) {
            return false;
        }
        for (String key : list) {
            if (content.contains(key)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断原始消息里是否有数组里符合的元素
     * 判断原始内容是否有集合的某个元素
     *
     * @param listContent 集合对象 判断的一方
     * @param raw_message 原始字符串 被判断
     * @return
     */
    @SuppressWarnings("all")
    public static boolean isContainsMessAge(List<String> listContent, String raw_message) {
        for (String s : listContent) {
            //左边的原始消息是否某个段落连续有集合内的某个关键元素
            //比如:集合内有你好,原始消息有你好世界,这里有连续的你好,就成立,反之不连续的不成立,亦或者是没有,也不成立
            if (raw_message.contains(s)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 过滤消息中的CQ码,如头部[CQ:我是CQ的内容]尾部
     * 过滤之后=头部尾部
     * 如果过滤之后还是跟原来的一样,就返回原来的字符串
     * 去除字符串中[CQ:....]中的CQ内容
     *
     * @param content 要过滤的字符串
     * @return 过滤之后的结果
     */
    public static String filtrationCQ(@NonNull String content) {
        String str = content.replaceAll("(\\[CQ:).*?(\\])", "");
        if (str.isEmpty()) {
            return "";
        }
        if (str.equals(content)) {
            return content;
        }
        return str;
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
     * 获取message中的json所有图片列表
     *
     * @param jsonArray 消息列表
     * @return 图片列表
     */
    public static List<JSONObject> getMessageImageList(@NonNull JSONArray jsonArray) {
        List<String> typeList = jsonArray.getByPath("type", List.class);
        List<JSONObject> dataList = jsonArray.getByPath("data", List.class);
        ArrayList<JSONObject> imageList = new ArrayList<>();
        for (int i = 0; i < typeList.size(); i++) {
            if (!("image".equals(typeList.get(i)))) {
                continue;
            }
            JSONObject entries = new JSONObject();
            JSONObject data = dataList.get(0);
            entries.set("file", data.get("file", String.class));
            entries.set("subType", data.get("subType", String.class));
            entries.set("url", data.get("url", String.class));
            imageList.add(entries);
        }
        return imageList;
    }

    /**
     * 判断是否是url链接
     * @param url url链接
     * @return 真亦或者假
     */
    public static boolean isHttpUrl(String url) {
        if (HttpUtil.isHttp(url) || HttpUtil.isHttps(url)) {
            return true;
        }
        return false;
    }


}
