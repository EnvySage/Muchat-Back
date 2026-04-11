package com.xs.chat.enumeration.permission;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;
@Getter
public enum GroupRoleEnum {
    OWNER("OWNER",3,"群主",
            EnumSet.allOf(GroupPermissionEnum.class)),
    ADMIN("ADMIN",2,"管理员",
            EnumSet.of(
                    GroupPermissionEnum.KICK_MEMBER,
                    GroupPermissionEnum.MUTE_MEMBER,
                    GroupPermissionEnum.UPDATE_GROUP_INFO,
                    GroupPermissionEnum.SEND_MESSAGE,
                    GroupPermissionEnum.UPDATE_SELF_NICKNAME,
                    GroupPermissionEnum.LEAVE_GROUP,
                    GroupPermissionEnum.VIEW_MEMBERS
            )),
    MEMBER("MEMBER",1,"成员",
            EnumSet.of(
                    GroupPermissionEnum.SEND_MESSAGE,
                    GroupPermissionEnum.UPDATE_SELF_NICKNAME,
                    GroupPermissionEnum.LEAVE_GROUP,
                    GroupPermissionEnum.VIEW_MEMBERS
            ));

    private final String code;
    private final Integer level;
    private final String message;
    private final Set<GroupPermissionEnum> permissions;

    GroupRoleEnum(String code, Integer level, String message, Set<GroupPermissionEnum> permissions) {
        this.code = code;
        this.level = level;
        this.message = message;
        this.permissions = permissions;
    }

    public boolean hasPermission(GroupPermissionEnum permission) {
        return permissions.contains(permission);
    }

    public boolean hasLevel(GroupRoleEnum role) {
        return level >= role.getLevel();
    }

    public static GroupRoleEnum fromCode(String code) {
        for (GroupRoleEnum value : GroupRoleEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
