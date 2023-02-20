package com.example.qqbot.Util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.data.bilibili.DataBiBiRoomLive;
import com.example.qqbot.data.json.DataJson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * 网络api封装工具
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/16 13:41
 */
public class NetworkUtil {


    /**
     * 翻译功能
     *
     * @param content 要翻译的文本
     * @return jsonOBj对象
     */
    public static JSONObject translate(String content) {
        Connection.Response response = SignalUtil.jsoupHttpGet("http://www.ggapi.cn/api/fanyi?type=AUTO&city=" + content, false);
        if (response.statusCode() != 200) {
            return SignalUtil.getJSONOBJNULL();
        }
        return JSONUtil.parseObj(response.body());
    }


    /**
     * 使用网络api解析蓝奏云链接,并获取直链
     *
     * @param url 蓝奏云分享链接的地址
     * @return 返回直链 字符串
     */
    public static String analyticStraightChainLanZouY(String url) {
        if (!(HttpUtil.isHttps(url) || HttpUtil.isHttp(url))) {
            return "";
        }
        //注意用的是自己的接口
        String content = HttpUtil.get("https://vip.mikuchase.ltd/?url=" + url);
        JSONObject jsonObject;
        try {
            jsonObject = JSONUtil.parseObj(content);
        } catch (Exception e) {
            return "";
        }
        Integer code = jsonObject.get("status", int.class);
        if (code == null || code == 0) {
            return "";
        }
        String downdLoadUrl = jsonObject.getByPath("info", String.class);
        if (downdLoadUrl == null) {
            return "";
        }
        return downdLoadUrl;
    }


    /**
     * 根据哔哩哔哩用户uid获取用户信息
     *
     * @param uid uid号
     * @return jsonobj对象
     */
    public static JSONObject getBiBiUserinfo(String uid) {
        JSONObject jsonnull = SignalUtil.getJSONOBJNULL();
        Connection.Response response = SignalUtil.jsoupHttpGet("https://api.xingzhige.com/API/b_personal/?mid=" + uid, false);
        if (response.statusCode() != 200) {
            return jsonnull;
        }
        String body = response.body();
        if (!(JSONUtil.isTypeJSONObject(body))) {
            return jsonnull;
        }
        JSONObject jsonObject = JSONUtil.parseObj(body);
        Integer code = jsonObject.get("code", int.class);
        if (code == null || code != 0) {
            return jsonnull;
        }
        JSONObject data = jsonObject.get("data", JSONObject.class);
        if (data == null || data.isEmpty()) {
            return jsonnull;
        }
        return data;
    }

    /**
     * 根据getBiBiUserinfo放的json对象进行解析排版
     *
     * @param jsonObject jsonobj对象
     * @return 格式化的内容
     */
    public static String getBiBiUserinfo(JSONObject jsonObject) {
        return CharSequenceUtil.format("""
                        用户uid:{}
                        用户昵称:{}
                        用户性别:{}
                        用户头像:{}
                        用户简介:{}
                        用户等级:{}
                        用户关注:{}
                        用户粉丝:{}
                        直播房间号:{}
                        直播房间标题:{}
                        直播房间地址:{}
                        直播房间封面:{}
                        直播房间人气:{}
                        """, jsonObject.getByPath("owner.mid", int.class)
                , jsonObject.getByPath("owner.name", String.class)
                , jsonObject.getByPath("owner.sex", String.class)
                , jsonObject.getByPath("owner.face", String.class)
                , jsonObject.getByPath("owner.desc", String.class)
                , jsonObject.getByPath("owner.level", String.class)
                , jsonObject.getByPath("fans.following", String.class)
                , jsonObject.getByPath("fans.follower", String.class)
                , jsonObject.getByPath("live_room.roomid", String.class)
                , jsonObject.getByPath("live_room.title", String.class)
                , jsonObject.getByPath("live_room.url", String.class)
                , jsonObject.getByPath("live_room.cover", String.class)
                , jsonObject.getByPath("live_room.watch.num", String.class)
        );
    }


    /**
     * 端口扫描
     * 扫描域名/服务器IP开放端口列表
     *
     * @param url 要扫苗的url
     * @return 相应内容
     */
    public static String portScan(String url) {
        if (!(InformationUtil.isHttpUrl(url))) {
            return "";
        }
        Connection.Response response = SignalUtil.jsoupHttpGet("https://v.api.aa1.cn/api/api-port/go.php?ip=" + url, false);
        if (response == null || response.statusCode() != 200) {
            return "";
        }
        return response.body();
    }


    private static JSONObject httpResponse(String url) {
        Connection.Response response = SignalUtil.jsoupHttpGet(url, false);
        if (response == null || response.statusCode() != 200) {
            return SignalUtil.getJSONOBJNULL();
        }
        String body = response.body();
        if (!(JSONUtil.isTypeJSONObject(body))) {
            return SignalUtil.getJSONOBJNULL();
        }
        return JSONUtil.parseObj(body);
    }


    /**
     * 获取随机谜语
     *
     * @return jsonobj对象
     */
    public static JSONObject riddle() {
        JSONObject jsonObject = httpResponse("https://v.api.aa1.cn/api/api-miyu/index.php");
        String code = jsonObject.get("code", String.class);
        if (!("1".equals(code))) {
            return SignalUtil.getJSONOBJNULL();
        }
        return jsonObject;
    }


    /**
     * 获取历史上的今天
     *
     * @return json对象
     */
    public static JSONObject getTodayInHistory() {
        JSONObject jsonObject = httpResponse("https://zj.v.api.aa1.cn/api/bk/?num=5&type=json");
        Integer code = jsonObject.get("code", int.class);
        if (code == null || code != 200) {
            return SignalUtil.getJSONOBJNULL();
        }
        return jsonObject;
    }


    /**
     * 获取B站热搜榜
     *
     * @return json对象
     */
    public static JSONArray getHotSearchListOfStationB(String user_id) {
        JSONObject jsonObject = httpResponse("https://v.api.aa1.cn/api/bilibili-rs/");
        Integer code = jsonObject.get("code", int.class);
        if (code == null || code != 1) {
            return SignalUtil.getJSONARRNULL();
        }
        JSONArray nodeArr = new JSONArray();
        nodeArr.add(DataJson.text(jsonObject.get("time", String.class)));
        JSONArray data = jsonObject.get("data", JSONArray.class);
        for (Object obj : data) {
            JSONObject datum = JSONUtil.parseObj(obj);
            String title = datum.get("title", String.class);
            String heat = datum.get("heat", String.class);
            String link = datum.get("link", String.class);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("标题:" + title + "\n");
            stringBuilder.append("热度:" + heat + "\n");
            stringBuilder.append("传送门:" + link);
            nodeArr.add(DataJson.text(stringBuilder));
        }
        return DataJson.nodeMerge("机器人", user_id, nodeArr);
    }

    /**
     * 获取微博热搜
     *
     * @param user_id 聊天记录发言人
     * @return jsonarr对象
     */
    public static JSONArray weiboHotSearchList(String user_id) {
        JSONObject jsonObject = httpResponse("https://zj.v.api.aa1.cn/api/weibo-rs/");
        Integer code = jsonObject.get("code", int.class);
        if (code == null || code != 1) {
            return SignalUtil.getJSONARRNULL();
        }
        JSONArray data = jsonObject.get("data", JSONArray.class);
        JSONArray nodeArr = new JSONArray();
        for (Object obj : data) {
            JSONObject datum = JSONUtil.parseObj(obj);
            String title = datum.get("title", String.class);
            String hot = datum.get("hot", String.class);
            String url = datum.get("url", String.class);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("标题:" + title + "\n");
            stringBuilder.append("热度:" + hot + "\n");
            stringBuilder.append("传送门:" + URLEncodeUtil.encode(url));
            nodeArr.add(DataJson.text(stringBuilder));
        }
        return DataJson.nodeMerge("机器人", user_id, nodeArr);
    }


    /**
     * 获取每日60秒看世界图片
     *
     * @param file 存储文件名
     * @return jsonObj对象
     */
    public static JSONObject getDay60World(String file) {
        return DataJson.imageUrl(file, "https://zj.v.api.aa1.cn/api/60s-old/", false);
    }


    /**
     * 获取人生倒计时内容
     *
     * @return 字符串结果
     */
    public static String getCountdownToLife() {
        JSONObject jsonObject = httpResponse("https://v.api.aa1.cn/api/rsdjs/");
        Integer code = jsonObject.get("code", int.class);
        if (code == null || code != 200) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n" + jsonObject.get("month") + "\n");
        stringBuilder.append(jsonObject.get("week") + "\n");
        stringBuilder.append(jsonObject.get("day") + "\n");
        stringBuilder.append(jsonObject.get("time"));
        return stringBuilder.toString();
    }


    /**
     * 获取疯狂星期四文案
     *
     * @return 文案内容
     */
    public static String getCrazyThursdayCopywriting() {
        JSONObject jsonObject = httpResponse("https://api.wqwlkj.cn/wqwlapi/kfcyl.php?type=json");
        if (jsonObject.isEmpty()) {
            return "";
        }
        Integer code = jsonObject.get("code", int.class);
        if (code == null || code != 1) {
            return "";
        }
        String msg = jsonObject.get("msg", String.class);
        if (msg == null) {
            return "";
        }
        return msg;
    }


    /**
     * 针对于该wqwlkjapi接口内获取json中的url做个封装,对应的接口传入对应的url即可
     *
     * @return 表情包url
     */
    public static String getWqwlkjImageUrl(String url) {
        JSONObject jsonObject = httpResponse(url);
        if (jsonObject.isEmpty()) {
            return "";
        }
        Integer code = jsonObject.get("code", int.class);
        if (code == null || code != 1) {
            return "";
        }
        String img = jsonObject.get("img", String.class);
        if (img == null) {
            return "";
        }
        return img;
    }

    /**
     * 获取指定用户动态信息
     *
     * @param uid 用户uid
     * @return jso动态内容和图片
     */
    public static JSONArray getAnalyzeTheDynamicApiOfStation(String uid) {
        JSONArray jsonArrNull = new JSONArray(0);
        JSONObject readJSONObject = httpResponse("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=" + uid);
        Integer code = readJSONObject.get("code", int.class);
        if (code == null || code != 0) {
            System.out.println("code错误!");
            return jsonArrNull;
        }
        JSONArray jsonArray = readJSONObject.getByPath("data.cards", JSONArray.class);
        if (jsonArray == null || jsonArray.isEmpty()) {
            return jsonArrNull;
        }
        ArrayList<JSONObject> dtArr = new ArrayList<>();
        for (Object o : jsonArray) {
            JSONObject item = JSONUtil.parseObj(o);
            JSONObject card = item.getByPath("card", JSONObject.class);
            if (card == null || card.isEmpty()) {
                continue;
            }
            dtArr.add(card);
        }
        JSONArray dtComtentArr = new JSONArray();
        for (JSONObject jsonObject : dtArr) {
            //动态类型是用户主动单独发的动态的,而非是回复动态
            String cdescription = jsonObject.getByPath("item.description", String.class);
            //动态的图片
            JSONArray pictures = jsonObject.getByPath("item.pictures", JSONArray.class);
            //动态类型是回复了对方的得类似于转发回复
            //String content = jsonObject.getByPath("item.content", String.class);
            //JSONObject origin = jsonObject.getByPath("origin", JSONObject.class);
            if (cdescription == null) {
                continue;
            }
            JSONObject dtMap = new JSONObject();
            dtMap.set("textContent", cdescription);
            JSONArray img_src = pictures.getByPath("img_src", JSONArray.class);
            if (img_src == null || img_src.isEmpty()) {
                continue;
            }
            dtMap.set("img_src", img_src);
            dtComtentArr.add(dtMap);
        }
        return dtComtentArr;
    }

    /**
     * 获取b站最新番剧信息
     * 并封装号对应的信息只需要用合并聊天记录对象发送即可
     *
     * @return 信息集合
     */
    public static JSONArray getTheLatestOpera() {
        JSONObject jsonObject = httpResponse("https://api.bilibili.com/pgc/web/timeline/v2?season_type=1&day_before=1&day_after=5");
        Integer code = jsonObject.get("code", int.class);
        if (code == null || code != 0) {
            return SignalUtil.getJSONARRNULL();
        }
        //最新番剧（内有预览图、标题、更新至第N话、更新时间等）
        JSONArray latest = jsonObject.getByPath("result.latest", JSONArray.class);

        List<String> title = latest.getByPath("title", List.class);
        //封面
        List<String> cover = latest.getByPath("cover", List.class);
        //ep地址
        List<Integer> episode_id = latest.getByPath("episode_id", List.class);
        //追番数
        List<String> follows = latest.getByPath("follows", List.class);
        //播放量
        List<String> plays = latest.getByPath("plays", List.class);
        //更新至第N话
        List<String> pub_index = latest.getByPath("pub_index", List.class);
        //存储每条消息的集合
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(DataJson.text("最新番剧"));
        for (int i = 0; i < title.size(); i++) {
            //一条消息中有的元素/内容
            //每条消息的样式集合
            JSONArray item = new JSONArray();
            //向该条消息中添加文本
            item.add(DataJson.text("番剧:" + title.get(i) + "\n"));
            String imageUrl = cover.get(i);
            //对该消息类进行追加图片
            item.add(DataJson.imageUrl(InformationUtil.subEqual("/", imageUrl), imageUrl, true));
            //向该条消息中追加添加文本
            item.add(DataJson.text("\n追番数:" + follows.get(i) + "\n"));
            //向该条消息中追加添加文本
            item.add(DataJson.text("播放量:" + plays.get(i) + "\n"));
            //向该条消息中追加添加文本
            item.add(DataJson.text("更新至:" + pub_index.get(i)));
            item.add(DataJson.text("啊b传送门:https://www.bilibili.com/bangumi/play/ep" + episode_id.get(i).toString()));
            //本条消息添加进去聊天记录集合
            jsonArray.add(item);
        }
        return jsonArray;
    }


    /**
     * 获取b站新番更新时间表
     * 并封装号对应的信息只需要用合并聊天记录对象发送即可
     *
     * @return JSONArray对象
     */
    public static JSONArray getXinfanTimetable() {
        JSONObject jsonObject = httpResponse("https://api.bilibili.com/pgc/web/timeline/v2?season_type=1&day_before=1&day_after=5");
        Integer code = jsonObject.get("code", int.class);
        if (code == null || code != 0) {
            return SignalUtil.getJSONARRNULL();
        }
        //为时间线，可以获取期当天更新的番剧信息，同上
        JSONArray timeline = jsonObject.getByPath("result.timeline", JSONArray.class);
        //近七天日期
        List<String> dateList = timeline.getByPath("date", List.class);
        //星期几
        List<Integer> day_of_weekList = timeline.getByPath("day_of_week", List.class);
        //对应是否是当天的内容0表示否.1表示是
        List<Integer> is_today = timeline.getByPath("is_today", List.class);
        //当日更新
        List<JSONArray> episodesList = timeline.getByPath("episodes", List.class);
        JSONArray nodeArr = new JSONArray();
        for (int i = 0; i < episodesList.size(); i++) {
            JSONArray tempJsonArr = JSONUtil.parseArray(episodesList.get(i));
            //每一个jsonArray对应当天的更新内容
            //番剧名
            List<String> title = tempJsonArr.getByPath("title", List.class);
            //封面
            List<String> cover = tempJsonArr.getByPath("cover", List.class);

            //ep地址
            List<Integer> episode_id = tempJsonArr.getByPath("episode_id", List.class);
            //追番数
            List<String> follows = tempJsonArr.getByPath("follows", List.class);
            //播放量
            List<String> plays = tempJsonArr.getByPath("plays", List.class);
            //更新至第N话
            List<String> pub_index = tempJsonArr.getByPath("pub_index", List.class);

            //一条消息的json样式
            JSONArray item = new JSONArray();
            if (is_today.get(i).equals(1)) {
                item.add(DataJson.text("今天日期:" + dateList.get(i) + "   " + InformationUtil.getDayWeek(day_of_weekList.get(i)) + "\n"));
            }
            item.add(DataJson.text("日期:" + dateList.get(i) + "\n"));
            item.add(DataJson.text("更新了" + title.size() + "个\n"));
            for (int y = 0; y < title.size(); y++) {
                if (title.isEmpty()) {
                    continue;
                }
                //处理对应当天的内容
                item.add(DataJson.text("\n番剧名:" + title.get(y) + "\n"));
                //插入封面
                String imageUrl = cover.get(y);
                item.add(DataJson.imageUrl(InformationUtil.subEqual("/", imageUrl), imageUrl, true));
                //插入追番数
                item.add(DataJson.text("\n追番数:" + follows.get(y) + "\n"));
                item.add(DataJson.text("播放量:" + plays.get(y) + "\n"));
                item.add(DataJson.text("更新至:" + pub_index.get(y) + "\n"));
                item.add(DataJson.text("啊b站传送门:https://www.bilibili.com/bangumi/play/ep" + episode_id.get(y).toString() + "\n"));
            }
            nodeArr.add(item);
        }
        return nodeArr;
    }


    /**
     * 获取喜加一
     *
     * @return 喜加一的资源类型
     */
    public static List<Map<String, String>> getXijiayi() {
        String content = HttpUtil.get("https://steamstats.cn/xi");
        if (content == null || content.isEmpty()) {
            return null;
        }
        Document parse = Jsoup.parse(content);
        List<Map<String, String>> list = new ArrayList<>();
        Elements tbodyTr = parse.getElementsByTag("tbody").get(0).getElementsByTag("tr");
        for (Element element : tbodyTr) {
            HashMap<String, String> map = new HashMap<>();
            Element title = element.getElementsByAttribute("title").get(0);
            //名
            String gameName = title.text();
            //地址
            String href = title.attr("href");
            Elements textNoWrap = element.getElementsByClass("text-no-wrap");
            //开始时间
            String startTime = textNoWrap.get(0).text();
            //结束时间
            String endTime = textNoWrap.get(1).text();
            //地址名
            String store = element.getElementsByClass("v-btn__content").text();
            map.put("gameName", gameName);
            map.put("href", href);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("store", store);
            list.add(map);
        }
        return list;
    }


    /**
     * 获取mikan网站Rss规则内容的资源
     *
     * @param parse RSS内容
     * @return jsonArr对象, 已经封装号text对象
     */
    public static JSONArray miKanRssRule(Document parse) {
        Elements item = parse.getElementsByTag("item");
        JSONArray jsonArray = new JSONArray(item.size());
        for (Element element : item) {
            //资源名
            String title = element.getElementsByTag("title").text();
            //资源大小(字节)
            String contentLength = element.getElementsByTag("contentLength").text();
            //时间
            String pubDate = element.getElementsByTag("pubDate").text();
            //磁力下载直链
            String url = element.select("enclosure[url]").attr("url");
            String format = CharSequenceUtil.format("""
                    资源名:{}
                    资源大小:{}
                    更新时间:{}
                    磁力下载:{}
                    """, title, InformationUtil.getSize(contentLength), pubDate, URLUtil.encode(url));
            jsonArray.add(DataJson.text(format));
        }
        return jsonArray;
    }


    /**
     * RSS订阅
     * 获取到mikan网站最新的资源
     *
     * @return jsonArr对象, 已经封装号text对象
     */
    public static JSONArray getMikanNewestList() {
        JSONArray jsonarrnull = SignalUtil.getJSONARRNULL();
        Connection.Response response = SignalUtil.jsoupHttpGet("https://mikanani.me/RSS/Classic", false);
        if (response == null || response.statusCode() != 200) {
            return jsonarrnull;
        }
        String body = response.body();
        if (body.isEmpty()) {
            return jsonarrnull;
        }
        return miKanRssRule(Jsoup.parse(body));
    }


    /**
     * RSS订阅方式搜索
     * 获取到mikan网站搜索内容
     *
     * @param key 要搜索的资源
     * @return jsonArr对象, 已经封装号text对象
     */
    public static JSONArray getMikanSearch(String key) {
        Connection connect = Jsoup.connect("https://mikanani.me/RSS/Search?searchstr=" + key);
        if (connect == null) {
            return SignalUtil.getJSONARRNULL();
        }
        try {
            return miKanRssRule(connect.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 搜索动漫花园的资源
     * 已经针对于机器人发送消息进行封装,直接调用nodeMessage即可
     *
     * @param key 关键词
     * @return
     */
    @SuppressWarnings("all")
    public static JSONArray getDMHYSearch(String key) {
        Connection.Response response = SignalUtil.jsoupHttpGet("https://dmhy.b168.net/topics/list?keyword=" + key, false);
        if (response == null) {
            return SignalUtil.getJSONARRNULL();
        }
        Document parse = Jsoup.parse(response.body());
        Elements tbodyTr = parse.getElementsByTag("tbody").get(0).getElementsByTag("tr");
        JSONArray jsonArray = new JSONArray(tbodyTr.size());
        for (Element element : tbodyTr) {
            String time = element.getElementsByTag("td").get(0).getElementsByTag("span").text();
            String title = element.getElementsByClass("title").text();
            String url = element.getElementsByClass("download-arrow arrow-magnet").attr("href");
            String size = element.select("td[align='center']").get(2).text();
            Elements td = element.getElementsByTag("td");
            //发布人
            String pushUser = td.get(td.size() - 1).text();
            String content = CharSequenceUtil.format("""
                    资源名:{}
                    发布时间:{}
                    文件大小:{}
                    发布人:{}
                    """, title, time, size, pushUser);
            jsonArray.add(DataJson.text(content));
            jsonArray.add(DataJson.text("下载地址(磁力):" + URLUtil.decode(url)));
        }
        return jsonArray;
    }


    /**
     * 获取萌番组最新种子资源
     *
     * @return 封装好的jsonarr对象
     */
    public static JSONArray getBangumiNewRss() {
        Connection.Response response = SignalUtil.jsoupHttpGet("https://bangumi.moe/rss/latest", false);
        if (response == null || response.statusCode() != 200) {
            return SignalUtil.getJSONARRNULL();
        }
        Document parse = Jsoup.parse(response.body());
        Elements item = parse.getElementsByTag("item");
        JSONArray jsonArray = new JSONArray(item.size());
        for (Element element : item) {
            String title = element.getElementsByTag("title").get(0).text();
            String pubDate = element.getElementsByTag("pubDate").get(0).text();
            String url = element.select("enclosure[url]").attr("url");
            String format = CharSequenceUtil.format("""
                    资源名:{}
                    发布时间:{}
                    下载地址:{}
                    """, title, pubDate, URLUtil.encode(url));
            jsonArray.add(DataJson.text(format));
        }
        return jsonArray;
    }


    /**
     * 获取指定b站直播间弹幕信息!
     *
     * @param id 直播间房号id
     * @return 封装的消息对象
     */
    public static Set<DataBiBiRoomLive> getBiBiliveMessage(String id) {
        Set<DataBiBiRoomLive> setNull = new HashSet<>(0);
        Connection.Response response = SignalUtil.jsoupHttpGet("http://api.live.bilibili.com/ajax/msg?roomid=" + id, true);
        if (response == null || response.statusCode() != 200) {
            return setNull;
        }
        JSONObject jsonBody;
        try {
            jsonBody = JSONUtil.parseObj(response.body());
        } catch (Exception e) {
            //throw new RuntimeException(e);
            return setNull;
        }
        Integer code = jsonBody.get("code", int.class);
        if (code == null || code != 0) {
            return setNull;
        }
        List<JSONObject> room = jsonBody.getByPath("data.room", List.class);
        Set<DataBiBiRoomLive> item = new HashSet<>();
        for (JSONObject v : room) {
            //用户名称
            String nickname = v.get("nickname", String.class);
            //用户uid
            String uid = v.get("uid", String.class);
            //发送时间
            String timeline = v.get("timeline", String.class);
            //用户发送内容
            String text = v.get("text", String.class);
            item.add(new DataBiBiRoomLive(nickname, uid, timeline, DateUtil.parse(timeline).getTime(), text));
        }
        return item;
    }


}



