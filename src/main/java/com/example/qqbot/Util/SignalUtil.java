package com.example.qqbot.Util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.data.DataUserEights;
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
    @Getter
    private static final JSONObject JSONOBJNULL = new JSONObject(0);

    /**
     * 空的jsonarr对象
     */
    @Getter
    private static final JSONArray JSONARRNULL = new JSONArray(0);


    /**
     * 群消息的终结点
     */
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
     * 获取图片信息终结点
     */
    private static final String GET_IMAGE = "/get_image";

    /**
     * 发送合并转发 ( 群 )
     */
    private static final String SEND_GROUP_FORWARD_MSG = "/send_group_forward_msg";

    /**
     * 发送合并转发 ( 好友 )终结点
     */
    private static final String SEND_PRIVATE_FORWARD_MSG = "/send_private_forward_msg";


    /**
     * 获取版本信息
     * 用于获取go-cqhttp版本等相关信息
     */
    private static final String GET_VERSION_INFO = "/get_version_info";

    /**
     * 获取群成员列表
     */
    private static final String GET_GROUP_MEMBER_LIST = "/get_group_member_list";


    private static final HashMap<String, String> headers = new HashMap<>();

    static {
        headers.put("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78");
    }


    private static JSONObject http(Connection data) {
        Connection.Response execute;
        try {
            execute = data.execute();
        } catch (IOException e) {
            log.info("请求execute时出错" + e.getMessage());
            return JSONOBJNULL;
        }
        int code;
        try {
            code = execute.statusCode();
        } catch (Exception e) {
            log.info("请求状态码异常!" + e.getMessage());
            return JSONOBJNULL;
        }
        if (code != 200) {
            return JSONOBJNULL;
        }
        return JSONUtil.parseObj(execute.body());
    }


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
        return http(data);
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
        return http(connection);
    }


    /**
     * 发送消息
     * 发送http post请求
     *
     * @param type    消息通道类型,如群聊私聊等
     * @param formMap 参数
     * @return jsonOBj对象
     */
    public static JSONObject httpPost(String type, Map<String, Object> formMap) {
        HttpResponse execute = HttpRequest.post(MailingAddress.SEND_MESSAGE + type)
                .method(Method.POST)
                .addHeaders(headers)
                .form(formMap)
                .execute();
        if (!(execute.getStatus() == 200)) {
            return JSONOBJNULL;
        }
        return JSONUtil.parseObj(execute.body());
    }


    /**
     * 发送http post请求
     *
     * @param type 消息通道类型,如群聊私聊等
     * @return jsonobj对象
     */
    public static JSONObject httpPost(String type) {
        Connection connection = Jsoup.connect(MailingAddress.SEND_MESSAGE + type)
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78")
                .ignoreContentType(true);
        return http(connection);
    }


    /**
     * 发送消息
     * 发送http post请求
     *
     * @param type      消息通道类型,如群聊私聊等
     * @param parameter 参数
     * @return jsonOBj对象
     */
    public static JSONObject httpPost(String type, String... parameter) {
        Connection connection = Jsoup.connect(MailingAddress.SEND_MESSAGE + type)
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78")
                .ignoreContentType(true)
                .data(parameter);
        return http(connection);


    }


    /**
     * 机器人发送群消息
     * 默认消息内容不做为纯文本,会解析json内容
     *
     * @param group_id 群号
     * @param message  消息内容
     * @return json对象结果
     */
    public static JSONObject sendGroupMessage(@NonNull String group_id, @NonNull String message) {
        return sendGroupMessage(group_id, message, false);
    }

    /**
     * 机器人发送群消息
     * 该方法需要手动指定消息内容是否作为纯文本发送
     *
     * @param group_id    群号
     * @param message     要发送的内容
     * @param auto_escape 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效,message为纯json内容时不生效
     * @return json对象结果
     */
    public static JSONObject sendGroupMessage(@NonNull String group_id, @NonNull String message, boolean auto_escape) {
        final Map<String, Object> data = new HashMap<>(2);
        data.put("group_id", group_id);
        data.put("message", message);
        data.put("auto_escape", auto_escape);
        return SignalUtil.httpPost(GROUPENDPOINT, data);
    }

    /**
     * 机器人发送群消息 发送到的群时默认的群聊 文件群（放文件）
     *
     * @param message 内容
     * @return
     */
    public static JSONObject sendGroupMessage(@NonNull String message) {
        return sendGroupMessage("528828094", message);
    }


    /**
     * 机器人发送群消息
     *
     * @param group_id 群号
     * @param message  消息内容
     * @return json对象结果
     */
    public static JSONObject sendGroupMessage(@NonNull String group_id, @NonNull JSONObject message) {
        return sendGroupMessage(group_id, message.toString());
    }

    /**
     * 机器人发送一条群消息
     *
     * @param group_id 群号
     * @param message  由json样式的类型组合成一条消息
     * @return json对象结果
     */
    public static JSONObject sendGroupMessage(@NonNull String group_id, @NonNull JSONArray message) {
        return sendGroupMessage(group_id, message.toString());
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
        return httpGet(GROUP_INFO_ENDPOINT, "?group_id=" + group_id + "&no_cache=" + no_cache);
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
        HashMap<String, Object> data = new HashMap<>(2);
        data.put("user_id", user_id);
        data.put("message", message);
        return SignalUtil.httpPost(PRIVATEENDPOINT, data);
    }

    /**
     * 给用户私发消息
     *
     * @return
     */
    public static JSONObject sendPrivateMessage(String user_id, JSONArray message) {
        return sendPrivateMessage(user_id, message.toString());
    }

    /**
     * 给用户私发消息
     *
     * @return
     */
    public static JSONObject sendPrivateMessage(String user_id, JSONObject message) {
        JSONArray jsonArray = new JSONArray(1);
        jsonArray.add(message);
        return sendPrivateMessage(user_id, jsonArray);
    }

    /**
     * 发送私聊消息,默认发给超级用户
     *
     * @param message 消息内容
     * @return jsonobj对象
     */
    public static JSONObject sendPrivateMessage(String message) {
        String admin = DataUserEights.SUPERUSER.get(0);
        return sendPrivateMessage(admin, message);
    }

    /**
     * 发送私聊消息,默认发给超级用户
     *
     * @param message 消息内容
     * @return jsonobj对象
     */
    public static JSONObject sendPrivateMessage(JSONArray message) {
        return sendPrivateMessage(message.toString());
    }

    /**
     * 发送私聊消息,默认发给超级用户
     *
     * @param message 消息内容
     * @return jsonobj对象
     */
    public static JSONObject sendPrivateMessage(JSONObject message) {
        JSONArray jsonArray = new JSONArray(1);
        jsonArray.add(message);
        return sendPrivateMessage(jsonArray);
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
        if (message_id == "") {
            return getJSONOBJNULL();
        }
        return SignalUtil.httpGet(GET_MSG, "?message_id=" + message_id);
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
        JSONObject json = httpGet(SET_GROUP_BAN, "?group_id=" + group_id + "&user_id=" + user_id + "&duration=" + duration);
        if (json.isEmpty()) {
            return JSONOBJNULL;
        }
        Integer retcode = json.get("retcode", int.class);
        if (retcode == null || !(retcode.equals(0))) {
            return JSONOBJNULL;
        }
        return json;
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


    /**
     * 获取图片消息
     *
     * @param fileName 图片缓存文件名
     * @return jsonobj对象
     */
    public static JSONObject getImage(String fileName) {
        return SignalUtil.httpPost(GET_IMAGE, "file=" + fileName);
    }


    /**
     * jsoupHttpGet的请求
     *
     * @param url             请求的url
     * @param followRedirects 是否跟随重定向
     * @return 响应体
     */
    public static Connection.Response jsoupHttpGet(String url, boolean followRedirects) {
        Connection.Response execute;
        try {
            execute = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78")
                    .ignoreContentType(true)
                    .followRedirects(followRedirects)
                    .execute();
        } catch (IOException e) {
            System.out.println("jsoupHttpGet请求出错!" + e.getMessage());
            return null;
            //throw new RuntimeException(e);
        }
        return execute;
    }


    /**
     * 发送合并转发 ( 群 )消息
     * 目前测试貌似是最多200条消息合并成聊天记录
     * 不过要求每条消息字符内容不要太多
     * <p>
     * 使用方法
     * <p>
     * 存储每条消息的集合
     * <p>
     * JSONArray jsonArray = new JSONArray();
     * <p>
     * 单独添加一条消息
     * <p>
     * jsonArray.add(DataJson.text("最新番剧"));
     * <p>
     * 一条消息中有的元素/内容-每条消息的样式集合
     * JSONArray item = new JSONArray();
     * <p>
     * 向该条消息中添加文本
     * item.add(DataJson.text("你好我是文本"));
     * <p>
     * 继续追加图片内容至对应消息
     * <p>
     * 对该消息类进行追加图片
     * item.add(DataJson.imageUrl("file","https/..xxxxxxjpg", true));
     * <p>
     * 向该条消息中追加添加文本
     * item.add(DataJson.text("我是追加的文本"));
     * <p>
     * 本条消息添加进去聊天记录集合
     * jsonArray.add(item);
     * <p>
     * 并消息成合并聊天记录
     * JSONArray nodeMerge = DataJson.nodeMerge("发送者昵称", "QQ号/可不填写真实", jsonArray);
     * <p>
     * 发送合并转发(群)消息
     * SignalUtil.sendGroupForwardMsg(nodeMerge);
     *
     * @param group_id  发送给的群号
     * @param jsonArray 消息的集合,内容内每一个元素对应一条消息
     * @return json响应体
     */
    public static JSONObject sendGroupForwardMsg(String group_id, JSONArray jsonArray) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("group_id", group_id);
        data.put("messages", jsonArray.toString());
        JSONObject httpPost = httpPost(SEND_GROUP_FORWARD_MSG, data);
        Integer retcode = httpPost.get("retcode", int.class);
        if (retcode.equals(100)) {
            log.info("发送失败!=" + httpPost.toStringPretty());
            return JSONOBJNULL;
        }
        return httpPost;
    }

    /**
     * 发送合并转发(群)消息,默认发送到文件群（放文件）群聊
     * 目前测试貌似是最多200条消息合并成聊天记录
     * 不过要求每条消息字符内容不要太多
     *
     * @param jsonArray 息的集合,内容内每一个元素对应一条消息
     * @return json响应体
     */
    public static JSONObject sendGroupForwardMsg(JSONArray jsonArray) {
        return sendGroupForwardMsg("528828094", jsonArray);
    }


    /**
     * 发送合并转发 ( 好友 )
     *
     * @param user_id  好友QQ号
     * @param messages 自定义转发消息(消息的合集)
     * @return json对象
     */
    public static JSONObject sendPrivateForwardMsg(String user_id, JSONArray messages) {
        return null;
    }


    /**
     * 获取机器人运行环境版本信息
     *
     * @return jsonobj对象
     */
    public static JSONObject get_version_info() {
        return httpPost(GET_VERSION_INFO);
    }

    /**
     * 获取群成员列表
     * 每个元素的内容和上面的 get_group_member_info 接口相同<br>
     * 但对于同一个群组的同一个成员, 获取列表时和获取单独的成员信息时<br>
     * 某些字段可能有所不同, 例如 area、title 等字段在获取列表时无法获得<br>
     * 具体应以单独的成员信息为准。
     *
     * @param group_id 群号
     * @param no_cache 是否不使用缓存（使用缓存可能更新不及时, 但响应更快
     * @return 响应内容为 json 数组,
     */
    public static JSONObject getGroupMemberList(String group_id, boolean no_cache) {
        HashMap<String, Object> map = new HashMap<>(2);
        map.put("group_id", group_id);
        map.put("no_cache", no_cache);
        return httpPost(GET_GROUP_MEMBER_LIST, map);
    }


}
