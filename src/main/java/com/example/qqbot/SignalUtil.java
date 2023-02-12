package com.example.qqbot;

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
            System.out.println(execute.url());
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
        HashMap<String, String> data = new HashMap<>(2);
        data.put("group_id", group_id);
        data.put("no_cache", String.valueOf(no_cache));
        JSONObject jsonObject = SignalUtil.httpGet(GROUP_INFO_ENDPOINT, data);
        if (jsonObject.isEmpty()) {
            return JSONNULL;
        }
        Integer retcode = jsonObject.get("retcode", int.class);
        if (retcode == null || retcode != 0) {
            return JSONNULL;
        }
        return jsonObject;
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
    public static JSONObject deleteMsg(String message_id) {
        return httpGet(DELETE_MSG, "?message_id=" + message_id);
    }

}
