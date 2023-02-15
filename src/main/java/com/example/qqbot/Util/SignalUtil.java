package com.example.qqbot.Util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.data.MailingAddress;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 机器人发送消息的静态方法
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/9 21:42
 */
@Slf4j
public class SignalUtil {

    /**
     * 空的jsonOBJ对象
     */
    private static final JSONObject JSONNULL = new JSONObject(0);


    /**
     * 群消息的终结点
     */
    @Getter
    private static final String GROUPENDPOINT = "/send_group_msg";


    /**
     * 获取群信息
     */
    private static final String GROUP_INFO_ENDPOINT = "/get_group_info";

    /**
     * 获取消息终结点
     */
    private static final String GET_MSG = "/get_msg";

    /**
     * 处理加群请求／邀请
     */
    private static final String SET_GROUP_ADD_REQUEST = "/set_group_add_request";


    /**
     * 发送私聊消息
     */
    @Getter
    private static final String PRIVATEENDPOINT = "/send_private_msg";


    /**
     * 发起撤回终结点
     */
    private static final String DELETE_MSG = "/delete_msg";


    /**
     * 群禁言终结点
     */
    private static final String SET_GROUP_BAN = "/set_group_ban";

    /**
     * 群组全员禁言
     */
    private static final String SET_GROUP_WHOLE_BAN = "/set_group_whole_ban";

    /**
     * get发送消息请求
     *
     * @param type      发送角色,私聊,群聊等
     * @param parameter 参数
     * @return 请求的相应结果json
     */
    public static JSONObject httpGet(String type, Map<String, String> parameter) {
        Connection data = Jsoup.connect(MailingAddress.SEND_MESSAGE + type)
                .method(Connection.Method.GET)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78")
                .ignoreContentType(true)
                .data(parameter);
        Connection.Response execute;
        try {
            execute = data.execute();
        } catch (IOException e) {
            log.info("请求execute时出错" + e.getMessage());
            return JSONNULL;
        }

        if (execute.statusCode() != 200) {
            return JSONNULL;
        }
        return JSONUtil.parseObj(execute.body());
    }

    /**
     * get发起通讯消息
     *
     * @param type      通讯类型
     * @param parameter 参数
     * @return json对象
     */
    public static JSONObject httpGet(String type, String parameter) {
        Connection connection = Jsoup.connect(MailingAddress.SEND_MESSAGE + type + parameter)
                .method(Connection.Method.GET)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78")
                .ignoreContentType(true);
        Connection.Response execute;
        try {
            execute = connection.execute();
        } catch (IOException e) {
            log.info("请求execute时出错" + e.getMessage());
            return JSONNULL;
        }
        int code = execute.statusCode();
        if (code != 200) {
            return JSONNULL;
        }
        return JSONUtil.parseObj(execute.body());
    }


    /**
     * 发送消息
     * 发送http post请求
     *
     * @param type      消息通道类型,如群聊私聊等
     * @param parameter 参数
     * @return jsonOBj对象
     */
    public static JSONObject httpPost(String type, Map<String, String> parameter) {
        Connection connection = Jsoup.connect(MailingAddress.SEND_MESSAGE + type)
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78")
                .ignoreContentType(true)
                .data(parameter);
        Connection.Response execute;
        try {
            execute = connection.execute();
        } catch (IOException e) {
            log.info("请求execute时出错" + e.getMessage());
            return JSONNULL;
        }
        int code;
        try {
            code = execute.statusCode();
        } catch (Exception e) {
            log.info("请求状态码异常!" + e.getMessage());
            return JSONNULL;
        }
        if (code != 200) {
            return JSONNULL;
        }

        return JSONUtil.parseObj(execute.body());
    }


    /**
     * 机器人发送群消息
     *
     * @param group_id 群号
     * @param message  消息内容
     * @return json对象结果
     */
    public static JSONObject sendGroupMessage(@NonNull String group_id, @NonNull String message) {
        final Map<String, String> data = new HashMap<>(2);
        data.put("group_id", group_id);
        data.put("message", message);
        return SignalUtil.httpGet(GROUPENDPOINT, data);
    }


    /**
     * 获取群的信息
     * 提示
     * 如果机器人尚未加入群, group_create_time, group_level, max_member_count 和 member_count 将会为0
     *
     * @param group_id 群号
     * @param no_cache 是否不使用缓存（使用缓存可能更新不及时, 但响应更快）
     * @return json对象
     */
    public static JSONObject getGroupInfo(String group_id, boolean no_cache) {
        return  httpGet(GROUP_INFO_ENDPOINT,"?group_id="+group_id+"&no_cache="+no_cache);
    }


    /**
     * 处理加群请求／邀请
     * 该 API 无响应数据
     *
     * @param flag    加群请求的 flag（需从上报的数据中获得）
     * @param type    add 或 invite, 请求类型（需要和上报消息中的 sub_type 字段相符）
     * @param approve 是否同意请求／邀请,默认true
     * @param reason  拒绝理由（仅在拒绝时有效）,需要传入一个具体的字符串,不可以为null
     * @return json对象
     */
    public static JSONObject setGroupAddRequest(String flag, String type, boolean approve, @NonNull String reason) {
        HashMap<String, String> data = new HashMap<>(4);
        data.put("flag", flag);
        data.put("type", type);
        data.put("approve", String.valueOf(approve));
        data.put("reason", reason);
        return SignalUtil.httpGet(SET_GROUP_ADD_REQUEST, data);
    }


    /**
     * 给用户私发消息
     *
     * @return
     */
    public static JSONObject sendPrivateMessage(String user_id, String message) {
        HashMap<String, String> data = new HashMap<>(2);
        data.put("user_id", user_id);
        data.put("message", message);
        return SignalUtil.httpGet(PRIVATEENDPOINT, data);
    }


    /**
     * 撤回消息
     *
     * @param message_id 消息 ID
     * @return 该 API 无响应数据
     */
    @SuppressWarnings("all")
    public static JSONObject deleteMsg(@NonNull String message_id) {
        return httpGet(DELETE_MSG, "?message_id=" + message_id);
    }


    /**
     * 根据消息ID获取原消息
     *
     * @param message_id 消息ID
     * @return json对象
     */
    @SuppressWarnings("all")
    public static JSONObject getMessage(@NonNull String message_id) {
        return SignalUtil.httpGet(SignalUtil.GET_MSG, "?message_id=" + message_id);
    }


    /**
     * 发起群指定成员禁言或接禁操作
     *
     * @param group_id 群号
     * @param user_id  要禁言的QQ号
     * @param duration 时间 禁言时长, 单位秒, 0 表示取消禁言
     * @return 该 API 无响应数据
     */
    public static JSONObject setGroupBan(@NonNull String group_id, @NonNull String user_id, @NonNull int duration) {
        return httpGet(SET_GROUP_BAN, "?group_id=" + group_id + "&user_id=" + user_id + "&duration=" + duration);
    }

    /**
     * 群组全体禁言
     *
     * @param group_id 群号
     * @param enable   是否禁言,默认true
     * @return json响应体
     */
    public static JSONObject set_group_whole_ban(String group_id, boolean enable) {
        return SignalUtil.httpGet(SET_GROUP_WHOLE_BAN, "?group_id=" + group_id + "&enable=" + enable);
    }

}