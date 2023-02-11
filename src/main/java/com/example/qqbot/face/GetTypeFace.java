package com.example.qqbot.face;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 16:56
 */
public interface GetTypeFace {

    /**
     * 根据内容返回指定类型的字符串值
     * @param content 内容
     * @return 类型
     */
    String getType(String content);
}
