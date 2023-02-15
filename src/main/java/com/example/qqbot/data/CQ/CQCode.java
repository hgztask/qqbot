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
     * @return
     */
    public static String image(String file, String url, boolean cache) {
        return CharSequenceUtil.format("[CQ:image,file={},url={},cache={}]", file, url, cache ? 1 : 0);
    }

}
