package com.example.qqbot.data.group;

import lombok.Data;

import java.util.Objects;

/**
 * 群成员列表数据层和群成员信息
 * 但对于同一个群组的同一个成员<br>
 * 获取列表时和获取单独的成员信息时<br>
 * 某些字段可能有所不同<br>
 * 例如 area、title 等字段在获取列表时无法获得<br>
 * 具体应以单独的成员信息为准
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/22 20:41
 */
@Data
public class DataGroupMember {

    /**
     * 群号
     */
    private String group_id;
    /**
     * QQ号
     */
    private String user_id;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 群名片-备注
     */
    private String card;
    /**
     * 性别, male 或 female 或 unknown
     */
    private String sex;
    /**
     * 年龄
     */
    private int age;

    /**
     * 加群时间戳
     */
    private int join_time;
    /**
     * 最后发言时间戳
     */
    private int last_sent_time;
    /**
     * 成员等级
     */
    private int level;
    /**
     * 角色, owner 或 admin 或 member
     */
    private String role;
    /**
     * 是否不良记录成员
     */
    private boolean unfriendly;
    /**
     * 地区
     */
    private String area;

    /**
     * 专属头衔
     */
    private String title;


    /**
     * 专属头衔过期时间戳
     */
    private int title_expire_time;
    /**
     * 是否允许修改群名片
     */
    private boolean card_changeable;
    /**
     * 禁言到期时间
     */
    private int shut_up_timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataGroupMember that = (DataGroupMember) o;
        return Objects.equals(group_id, that.group_id) && Objects.equals(user_id, that.user_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group_id, user_id);
    }
}
