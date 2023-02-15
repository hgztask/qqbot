package com.example.qqbot.data.CQ;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;

/**
 * CQ码
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/15 11:13
 */
public class CQCode {


    /**
     * 返回图片的CQ码
     *
     * @param file  文件名
     * @param url   网络地址
     * @param cache 只在通过网络 URL 发送时有效, 表示是否使用已缓存的文件, 默认为1,即默认true
     * @return CQ码
     */
    public static String image(String file, String url, boolean cache) {
        return CharSequenceUtil.format("[CQ:image,file={},url={},cache={}]", file, url, cache ? 1 : 0);
    }


    /**
     * 艾特成员或者全体成员
     *
     * @param user_id @的 QQ 号, all 表示全体成员
     * @param name    当在群中找不到此QQ号的名称时才会生效
     * @return CQ码
     */
    public static String at(String user_id, String name) {
        return CharSequenceUtil.format("[CQ:at,qq={},name={}]", user_id, name);
    }

    /**
     * QQ表情
     * 常用ID
     * 滑稽脸 178
     * 更多可前往QQ标签ID表https://github.com/kyubotics/coolq-http-api/wiki/表情-CQ-码-ID-表
     *
     * @param id QQ表情的ID
     * @return CQ码
     */
    public static String face(String id) {
        return CharSequenceUtil.format("[CQ:face,id={}]", id);
    }

    /**
     * 链接分享
     *
     * @param url   url链接
     * @param title 标题
     * @return CQ码
     */
    public static String share(String url, String title) {
        return CharSequenceUtil.format("[CQ:share,url={},title={}]", url, title);
    }


    /**
     * 音乐分享
     *
     * @param type qq 163 xm(分别表示使用 QQ 音乐、网易云音乐、虾米音乐)
     * @param id   歌曲ID
     * @return CQ码
     */
    public static String music(String type, String id) {
        return CharSequenceUtil.format("[CQ:music,type={},id={}]", type, id);
    }

    /**
     * 音乐自定义分享
     *
     * @param url      点击后跳转目标 URL
     * @param audioUrl 音乐 的直链URL
     * @param title    标题
     * @param content  发送时可选, 内容描述
     * @param imageUrl 发送时可选, 图片 URL
     * @return CQ码
     */
    public static String customMusic(String url, String audioUrl, String title, String content, String imageUrl) {
        return CharSequenceUtil.format("[CQ:music,type=custom,url={},audioUrl={},title={},content={},imageUrl={}]", url, audioUrl, title
                , content, imageUrl);
    }


}
