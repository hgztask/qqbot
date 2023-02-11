package com.example.qqbot.data;

import cn.hutool.core.collection.ListUtil;
import lombok.Data;

import java.util.List;

/**
 * 用户权限
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 13:52
 */
public class DataUserEights {
    /**
     * 超级用户权限
     */
    public static final List<String> SUPERUSER = ListUtil.toList("2978778354");

    /**
     * 管理员权限
     */
    public static final List<String> ADMINISTRATORS = ListUtil.toList("");


}
