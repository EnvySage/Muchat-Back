package com.xs.chat.enumeration.permission;

import lombok.Getter;

@Getter
public enum GroupPermissionEnum {
    // ===== 群主权限 =====
    DISSOLVE_GROUP("DISSOLVE_GROUP", "解散群聊"),
    TRANSFER_OWNER("TRANSFER_OWNER", "转让群主"),
    MANAGE_ADMIN("MANAGE_ADMIN", "设置/取消管理员"),
    // ===== 管理权限 =====
    KICK_MEMBER("KICK_MEMBER", "踢出成员"),
    MUTE_MEMBER("MUTE_MEMBER", "禁言/解禁成员"),
    UPDATE_GROUP_INFO("UPDATE_GROUP_INFO", "修改群信息"),
    INVITE_MEMBER("INVITE_MEMBER", "邀请成员"),

    // ===== 基础权限 =====
    SEND_MESSAGE("SEND_MESSAGE", "发送消息"),
    UPDATE_SELF_NICKNAME("UPDATE_SELF_NICKNAME", "修改群昵称"),
    LEAVE_GROUP("LEAVE_GROUP", "退出群聊"),
    VIEW_MEMBERS("VIEW_MEMBERS", "查看成员列表");

    private final String code;
    private final String desc;

    GroupPermissionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
