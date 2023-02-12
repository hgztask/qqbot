package com.example.qqbot.Util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.data.group.DataGroup;
import lombok.NonNull;

/**
 * 成员信息处理
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 22:29
 */
public class UserInfoUtil {


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
     * 责获取被艾特方的QQ号码
     * @param dataGroup  dataGroup对象
     * @return 返回QQ号字符串或者空的字符串
     */
    public static @NonNull String getUserATID(@NonNull DataGroup dataGroup) {
        return UserInfoUtil.getUserATID(JSONUtil.parseObj(dataGroup));
    }


}
