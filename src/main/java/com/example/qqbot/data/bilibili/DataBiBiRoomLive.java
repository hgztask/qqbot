package com.example.qqbot.data.bilibili;

import lombok.Data;

import java.util.Objects;

/**
 *
 * b站直播间用户发言数据
 * @author byhgz
 * @version 1.0
 * @date 2023/2/20 14:20
 */
@Data
public class DataBiBiRoomLive {
    /**
     * 发言人
     */
    private String nickname;
    /**
     * 发言人uid
     */
    private String uid;
    /**
     * 发送时间
     */
    private String timeline;
    /**
     * 时间戳
     */
    private long time;
    /**
     * 发送内容
     */
    private String text;


    public DataBiBiRoomLive(String nickname, String uid, String timeline, long time, String text) {
        this.nickname = nickname;
        this.uid = uid;
        this.timeline = timeline;
        this.time = time;
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataBiBiRoomLive that = (DataBiBiRoomLive) o;
        return time == that.time && Objects.equals(nickname, that.nickname) && Objects.equals(uid, that.uid) && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, uid, time, text);
    }
}
